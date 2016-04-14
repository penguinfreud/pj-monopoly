package monopoly.place;

import java.util.Scanner;

public interface PlaceReader {
    Place read(GameMapReader reader, Scanner sc) throws Exception;
}
