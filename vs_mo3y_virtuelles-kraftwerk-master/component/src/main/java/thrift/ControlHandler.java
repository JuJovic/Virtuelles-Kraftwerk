package src.main.java.thrift;

import org.apache.thrift.TException;
import src.main.java.components.Erzeuger;



public class ControlHandler implements ComponentController.Iface {

  private Erzeuger erzeuger;

  public ControlHandler(Erzeuger erzeuger) {
    this.erzeuger = erzeuger;
  }

  @Override
  public boolean setPower(double power) throws TException {
    if (power < 0 || power > 1) {
      // only values from 0 (0%) to 1 (100%) are valid
      return false;
    }
    erzeuger.setPower(power);
    return true;
  }

}


