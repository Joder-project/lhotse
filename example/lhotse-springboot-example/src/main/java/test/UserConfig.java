package test;

import org.lhotse.config.core.FieldConvertor;
import org.lhotse.config.core.IConfig;
import org.lhotse.config.core.annotations.Custom;
import org.lhotse.config.core.annotations.StorageConfig;
import org.lhotse.config.spring.GenerateStorage;

import javax.annotation.Nonnull;
import java.util.Random;

@GenerateStorage
@StorageConfig(path = "user-config.xlsx")
public record UserConfig(Integer id, String name,
                         @Custom(convertor = RandomGenerate.class) Random random
) implements IConfig<Integer> {

    public static class RandomGenerate implements FieldConvertor {

        @Override
        public Object encode(@Nonnull String text) {

            return new Random(Integer.parseInt(text));
        }
    }
}
