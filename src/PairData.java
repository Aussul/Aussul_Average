import java.math.BigDecimal;
import java.util.List;
import javax.swing.table.AbstractTableModel;


public class PairData extends AbstractTableModel implements Comparable<PairData> {
    
    public String pair;
    public List<String[]> data;
    public BigDecimal fee=BigDecimal.ZERO;
    public BigDecimal feeCost=BigDecimal.ZERO;      
    public BigDecimal feeBuyCost=BigDecimal.ZERO;
    public BigDecimal feeSellCost=BigDecimal.ZERO;
    public BigDecimal custom_Fee_Percentage;
    public boolean save;

    
    public PairData(String pair , List<String[]> data , BigDecimal custom_Fee_Percentage , boolean save) {        
       super();
       this.pair = pair;
       this.data = data;
       this.custom_Fee_Percentage = custom_Fee_Percentage;
       this.save = save;
        
    }
    
    public int getRowsCount() {
         return data.size();
    }
        
    @Override
    public int getRowCount() {
         return Integer.max(data.size()+2, 25);
    }

    @Override
    public int getColumnCount() {
        return Main.headers.length;
    }

    @Override
    public String getColumnName(int column) {
        return Main.headers[column];
    }
        
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(rowIndex >= data.size() || columnIndex >= data.get(rowIndex).length)
            return null;
     
        return data.get(rowIndex)[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {        
        return rowIndex <= data.size();
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {        
        PairDataUtils.setValueAt(this, aValue, rowIndex, columnIndex);

    }

    @Override
    public int compareTo(PairData pd) {
        return (this.pair).compareTo(pd.pair);
    }
    
}
