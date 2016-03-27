package bot.strategies;

import lombok.Data;
import field.Field;

@Data
public class ForcedResult {
    private final boolean forcedCapture;
    private final Field resultingField;
    private final int forcedMovesCount;
}
