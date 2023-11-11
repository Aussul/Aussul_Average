import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;


public class ListPairs extends AbstractListModel<String> {
    
    public List<PairData> pairData = new ArrayList<>();
    public String exchangeName;
    public String fee_Currency;
    public String last_Order;
    public BigDecimal default_Fee_Percentage;
    public boolean is_Fee_Calculated = false;
    
    public ListPairs(String exchangeName) {
       this.exchangeName = exchangeName;
       this.fee_Currency="noFeeCurrancy";
       this.default_Fee_Percentage = BigDecimal.ZERO;
       this.last_Order = Main.sub; 
    }
    
    public ListPairs(String exchangeName, String fee_Currency, BigDecimal default_Fee_Percentage, String last_Order) {
       this.exchangeName = exchangeName;
       this.fee_Currency=fee_Currency;
       this.default_Fee_Percentage = default_Fee_Percentage;        
       this.last_Order = last_Order; 
    }
        
    @Override
    public int getSize() { 
        return pairData.size();
    }
    
    @Override
    public String getElementAt(int i) { 
        return pairData.get(i).pair; 
    }

    public PairData getPairData(String pair) { 
        for (PairData pd : pairData) {
            if(pair.equals(pd.pair))
                return pd;
        }
        
        return null;
    }
}
