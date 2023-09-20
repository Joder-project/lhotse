package org.lhotse.config.spring;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("org.lhotse.config.spring.GenerateStorage")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class SpringStorageProcessor extends AbstractProcessor {

    private Filer filer;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        var set = new HashSet<>(super.getSupportedAnnotationTypes());
        set.add(GenerateStorage.class.getCanonicalName());
        set.add(EnableLhotse.class.getCanonicalName());
        return set;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var mainClass = roundEnv.getElementsAnnotatedWith(EnableLhotse.class).stream().findFirst()
                .filter(e -> e.getKind() == ElementKind.CLASS)
                .map(e -> {
                    var name = e.toString();
                    return name.substring(0, name.lastIndexOf("."));
                })
                .orElse(null);
        var elements = roundEnv.getElementsAnnotatedWith(GenerateStorage.class).stream()
                .filter(e -> e.getKind() == ElementKind.RECORD)
                .collect(Collectors.toSet());
        createConfiguration(elements, mainClass);
        return true;
    }

    void createConfiguration(Set<? extends Element> elements, String basePackage) {
//        basePackage = elements.stream().findFirst()
//                .map(e -> {
//                    var name = e.toString();
//                    return name.substring(0, name.lastIndexOf("."));
//                })
//                .orElse("org.lhotse.config.spring");
        if (basePackage == null) {
            return;
        }
        StringBuilder sb = new StringBuilder()
                .append(String.format("""
                        package %s;
                                          
                        import java.util.*;
                        import org.lhotse.config.core.*;
                        import org.springframework.context.annotation.*;
                        import org.springframework.boot.autoconfigure.condition.*;
                                                
                        @Configuration
                        class SpringStorageGeneratorConfiguration {
                            
                        """, basePackage));
        List<String> classes = new ArrayList<>();
        for (Element element : elements) {
            sb.append(buildBeanMethod(((TypeElement) element), classes));
        }
        sb.append(String.format("\tprivate final Set<Class<?>> registerClasses = Set.of(%s);\n", String.join(", ", classes)));
        sb.append("""
                    
                    SpringStorageGeneratorConfiguration(GlobalDataStorage manager) {
                        manager.init(registerClasses);
                    }
                    
                """);
        sb.append("}\n");
        try {
            JavaFileObject source = filer.createSourceFile(basePackage + ".SpringStorageGeneratorConfiguration");
            Writer writer = source.openWriter();
            writer.write(sb.toString());
            writer.flush();
            writer.close();
        } catch (Exception e) {

        }
    }

    String buildBeanMethod(TypeElement element, List<String> classes) {
        String id = element.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> ((ExecutableElement) e))
                .filter(e -> "id".equals(e.getSimpleName().toString()))
                .map(e -> e.getReturnType().toString())
                .findFirst()
                .orElse("");
        String type = element.getQualifiedName().toString();
        String simpleName = element.getSimpleName().toString();
        classes.add(type + ".class");
        if (id.isEmpty()) {
            return String.format("""
                                            
                        @Bean
                        SingleStorage<%s> %sSingleStorage(StorageFactory factory){
                            return factory.createSingle(%s.class);
                        }
                    """, type, simpleName, type);
        } else {
            return String.format("""
                                        
                    @Bean
                        Storage<%s, %s> %sStorage(StorageFactory factory){
                            return factory.create(%s.class);
                    }
                """, id, type, simpleName, type);
        }
    }
}
