package src.main.java.components;

import java.time.LocalDateTime;
import src.main.java.components.Component;

public class Verbraucher_Fabrik extends Component {

  public Verbraucher_Fabrik(int ID, String name) {
    super(ID, name, true);
  }

  int getKW(LocalDateTime timestamp) {
    double randomFactor = getRandomFactor(0.9, 1.25);
    if (timestamp.getHour() >= 6 && timestamp.getHour() <= 20) {
      return (int) (randomFactor * 30000);
    }
    return (int) (randomFactor * 10000);
  }
}

