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
public class RewardTypesConverter implements AttributeConverter<List<String>, String> {
    
    private static final String SEPARATOR = ", ";

    @Override
    public String convertToDatabaseColumn(List<String> rewardTypes) {
        return StringUtils.join(SEPARATOR, rewardTypes);
    }

    @Override
    public List<String> convertToEntityAttribute(String rewardTypesString) {
        return Arrays.asList(StringUtils.splitPreserveAllTokens(rewardTypesString, ","));
    }
    
}
