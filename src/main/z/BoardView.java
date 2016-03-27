package z;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BoardView {
    private final String label;
    List<List<Cell>> cells;
}
