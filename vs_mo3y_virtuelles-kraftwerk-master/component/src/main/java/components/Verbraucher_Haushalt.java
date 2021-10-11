package src.main.java.components;

import java.time.LocalDateTime;

public class Verbraucher_Haushalt extends Component {

  //Simuliert 1000 typische haushälter
  public Verbraucher_Haushalt(int ID, String name) {
    super(ID, name, true);
  }

	//10000 * typischer haushalt
  int getKW(LocalDateTime timestamp) {
    double randomFactor = getRandomFactor(0.9, 1.1);
    switch (timestamp.getHour()) {
      case 0:
        return (int) (randomFactor * 7000);
      case 1:
        return (int) (randomFactor * 7500);
      case 2:
        return (int) (randomFactor * 6750);
      case 3:
        return (int) (randomFactor * 6750);
      case 4:
        return (int) (randomFactor * 6500);
      case 5:
        return (int) (randomFactor * 6500);
      case 6:
        return (int) (randomFactor * 6500);
      case 7:
        return (int) (randomFactor * 8750);
      case 8:
        return (int) (randomFactor * 11000);
      case 9:
        return (int) (randomFactor * 13000);
      case 10:
        return (int) (randomFactor * 8500);
      case 11:
        return (int) (randomFactor * 6500);
      case 12:
        return (int) (randomFactor * 6000);
      case 13:
        return (int) (randomFactor * 6000);
      case 14:
        return (int) (randomFactor * 5750);
      case 15:
        return (int) (randomFactor * 5750);
      case 16:
        return (int) (randomFactor * 6500);
      case 17:
        return (int) (randomFactor * 7000);
      case 18:
        return (int) (randomFactor * 9000);
      case 19:
        return (int) (randomFactor * 15000);
      case 20:
        return (int) (randomFactor * 20000);
      case 21:
        return (int) (randomFactor * 20000);
      case 22:
        return (int) (randomFactor * 13500);
      case 23:
        return (int) (randomFactor * 9500);
      default:
        return 0;
    }
  }
}

