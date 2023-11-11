import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;



public class Exchanges extends DefaultComboBoxModel{

    
    public static List<ListPairs> listExchange = new ArrayList();
    
    public void addExchange(String exchangeName, String fee_Currency, BigDecimal default_Fee_Percentage, String last_Order) {
        listExchange.add(new ListPairs(exchangeName,fee_Currency,default_Fee_Percentage,last_Order));
    }    
    
    public ListPairs getExchange(String exchangeName) {
       
        if(exchangeName != null) {
            for (ListPairs lp : listExchange) {
                if(exchangeName.equals(lp.exchangeName))
                    return lp;
            }
        }
        return null;
    } 
    
    public ListPairs getExchange(int exchange) {
        return listExchange.get(exchange);
    }

    @Override
    public void addElement(Object a) {
        listExchange.add(new ListPairs((String) a));
    }

    @Override
    public int getIndexOf(Object o) {
        for (ListPairs l : listExchange) {
            if(l.exchangeName.equals(o))
                return listExchange.indexOf(l);
        }
        return -1;
    }

    @Override
    public Object getElementAt(int index) {
        if(index > listExchange.size()-1)
            return "<Add New - Delete>";
        return listExchange.get(index).exchangeName;
    }

    @Override
    public int getSize() {
        return listExchange.size()+1;
    }

    @Override
    public Object getSelectedItem() {
        return super.getSelectedItem();
    }

    @Override
    public void removeElementAt(int index) {
       listExchange.remove(index);
    }
    
    
    public boolean removeElement(String s) {
        for (ListPairs l : listExchange) {
            if(l.exchangeName.equals(s))
                return listExchange.remove(l);
        }
        return false;
    }    
    
    
}
