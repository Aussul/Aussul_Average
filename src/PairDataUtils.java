import java.util.Arrays;
import java.util.Calendar;



public class PairDataUtils {

    
    private PairDataUtils() {
    }
   
    
    public static void setValueAt(PairData p, Object aValue, int rowIndex, int columnIndex) {
            
        if(!aValue.equals("")) {
            if(rowIndex >= p.data.size()) {
                rowIndex = p.data.size();
                p.data.add(new String[p.getColumnCount()]);            
            }
            String value,c;
            value = aValue.toString();
            c = p.getColumnName(columnIndex);
            
            if(value.contains(","))
	       value = value.replaceAll(",", "");
				
            if(c.equals(Main.hdr_Date)) {
                if(value.equalsIgnoreCase("d")) { //print current date and time
                    value = Main.sdf.format(Calendar.getInstance().getTime());
                }
                    
            }else
              value = value.toUpperCase();
            
            if(value.matches("[.]\\d+") && // .123 to 0.123
              (c.equals(Main.hdr_Quantity) || 
               c.equals(Main.hdr_Amount) || 
               c.equals(Main.hdr_Fee) ||
               c.equals(Main.hdr_Price)))
                    value = "0"+value;
            
            if(c.equals(Main.hdr_Side)) {
                if(value.startsWith("S"))
                   value=Main.sell;
                else if(value.startsWith("B"))
                   value=Main.buy;
                else {
                    Gui.editCellError(0,rowIndex,columnIndex);
                    return;
                }
            }
            
            else if(c.equals(Main.hdr_Price) && (!value.matches(Main.value_Int) && !value.equals(Main.sub))) {
                
                Gui.editCellError(1,rowIndex,columnIndex);
                return;
            }
            
            else if((c.equals(Main.hdr_Quantity) || 
                     c.equals(Main.hdr_Amount) || 
                     c.equals(Main.hdr_Fee)) &&
                     !value.matches(Main.value_And_Currancy)) {
            
                    if(c.equals(Main.hdr_Fee) && value.equals(Main.sub)) {
                        //continue
                    
                    }else if(rowIndex != 0 && value.matches(Main.value_Int)) {
                        String value2 = p.data.get(0)[columnIndex];
                        if(value2 != null && value2.matches(Main.value_And_Currancy)){
                            value = value + value2.replaceAll("[0-9.]", "");
                        }else {
                            Gui.editCellError(2,rowIndex,columnIndex);
                            return;
                        }
                    }else {
                        Gui.editCellError(2,rowIndex,columnIndex);
                        return;
                    }          
            }

           if(value.equals(p.data.get(rowIndex)[columnIndex]))
               return;
           

               
               
           p.data.get(rowIndex)[columnIndex] = value;
           
           
               
           
           for (int i=1 ; i < p.getColumnCount() ; i++){ //Check data ( ignore date ) 
                if(p.data.get(rowIndex)[i] == null)
                    return;                   
            }

           if(!c.equals(Main.hdr_Date))
                Main.refreshData(p , c.equals(Main.hdr_Fee)|| c.equals(Main.hdr_Amount)|| c.equals(Main.hdr_Quantity) || c.equals(Main.hdr_Side));
           p.save=true;
        
        }else if (rowIndex < p.data.size()) {

             p.data.get(rowIndex)[columnIndex]=null;
             if(Arrays.equals(p.data.get(rowIndex), new String [] {null,null,null,null,null,null})) {
                p.data.remove(rowIndex);
             }
        }
            
    }
    
    public static void moveRowUp(PairData p, int rowIndex) {
        
            String[] mdata = p.data.remove(rowIndex);
            p.data.add(rowIndex-1, mdata);
            p.fireTableRowsUpdated(rowIndex-1, rowIndex);
    }
    
    public static void moveRowDown(PairData p, int rowIndex) {

            String[] mdata = p.data.remove(rowIndex);
            p.data.add(rowIndex+1, mdata);
            p.fireTableRowsUpdated(rowIndex, rowIndex+1);
    }    
    
    public static void removeRow(PairData p, int rowIndex) {
            p.data.remove(rowIndex);
            p.fireTableRowsDeleted(rowIndex, rowIndex);
    } 


    
}
