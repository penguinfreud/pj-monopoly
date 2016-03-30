package monopoly;

import java.util.Scanner;

public interface PlaceReader {
    Place read(MapReader reader, Scanner sc) throws Exception;
}
