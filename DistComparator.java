import java.util.Comparator;

public class DistComparator implements Comparator<Viewable>{
  
  @Override
  public int compare(Viewable o1, Viewable o2) {
    return Double.compare(o2.getD(), o1.getD());
  }
  
}
