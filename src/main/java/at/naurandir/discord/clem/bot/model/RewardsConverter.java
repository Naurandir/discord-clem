package at.naurandir.discord.clem.bot.model;

import java.util.Arrays;
import java.util.List;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author aspiel
 */
@Converter
public class RewardsConverter implements AttributeConverter<List<String>, String> {
    
    private static final String SEPARATOR = ", ";

    @Override
    public String convertToDatabaseColumn(List<String> rewards) {
        return StringUtils.join(SEPARATOR, rewards);
    }

    @Override
    public List<String> convertToEntityAttribute(String rewardsString) {
        return Arrays.asList(StringUtils.splitPreserveAllTokens(rewardsString, ","));
    }
    
}
