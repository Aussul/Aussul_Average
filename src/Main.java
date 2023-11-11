
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    
    public static final String programName = "Aussul_Average";// v1.0
    public static Exchanges exchanges = new Exchanges();
    public static final String hdr_Date = "Date/Note";
    public static final String hdr_Side = "Side";
    public static final String hdr_Price = "Price";
    public static final String hdr_Quantity = "Quantity";
    public static final String hdr_Amount = "Amount";
    public static final String hdr_Fee = "Fee"; 
    public static final String sell = "SELL";
    public static final String buy = "BUY";
    public static final String sub = "-";
    public static final String value_Int = "\\d+|\\d+.\\d+";
    public static final String value_And_Currancy = "(\\d+|\\d+.\\d+)([A-Z]+|\\p{Sc})";
    public static final String[] headers = new String [] {hdr_Date , hdr_Side , hdr_Price , hdr_Quantity , hdr_Amount , hdr_Fee};
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
    public static Gui gui;
    public static Main m;
    public static ListPairs lp;
    public final String exchangesHeader = "Exchange,Fee Currancy,Fee Percentage,Last order";
    public final String path = System.getProperty("user.home")+ File.separator + programName + File.separator;
    public final String extension = ".csv";
    public final String pathExchangesFile = path+"Exchanges"+extension;    
    final String split = ",";
    final String autoAdded = "Auto added Fee ";   
    String lastExchange=null;
    String pair,currancy,reference_Currancy,fee_Currancy;
    File f = null;
    File lf[] = null;
    Scanner sc;    
    BigDecimal all_Buy,all_Sell,quantity_Buy,quantity_Sell,quantity,average,all_Time_average,total,fee,fee_Percentage;
    int date_column,side_column,quantity_column,amount_column,fee_column,price_column;
    List<String[]> data;        
    PrintWriter pw;
    StringBuilder sp;
    

    

    public static void main(String args[]) {

        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | 
                 InstantiationException | 
                 IllegalAccessException | 
                 javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Gui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }


        
        m = new Main();
        m.init();
        gui = new Gui(m);
        
        java.awt.EventQueue.invokeLater(() -> {
            gui.setVisible(true);
        });      
        
    }
   
   public void init() {
      try {
       f = new File(path);
       if(!f.exists())
           f.mkdir();
       
       f = new File(pathExchangesFile);
       if(f.exists()) {
           String line=null,c[];
           sc = new Scanner(f);
           if(sc.hasNextLine())
                line = sc.nextLine();
           if(exchangesHeader.equals(line))
                while (sc.hasNextLine()) {
                    line = sc.nextLine();

                    if (line.contains(split)) { // Binance,BNB,0.0750,-
                       c = line.split(split);
                       if(c[0].startsWith(sub)) {
                           c[0] = c[0].replaceFirst(sub, "");
                           lastExchange = c[0];
                       }
                       if(c.length == 4 && c[2].matches("[-0-9.]+")) {
                        fee_Percentage = BigDecimal.valueOf(Double.parseDouble(c[2]));
                        if(fee_Percentage.signum() > 0)
                            fee_Percentage = fee_Percentage.movePointLeft(2);

                        exchanges.addExchange(c[0],c[1],fee_Percentage,c[3]);

                       } 
                    }
                }                        
        }else
          f.createNewFile(); 
       
       if(Exchanges.listExchange.isEmpty()) { //Defult
           exchanges.addExchange("Binance","BNB", BigDecimal.valueOf(0.000750),sub);
           exchanges.addExchange("Kucoin","USDT", BigDecimal.valueOf(0.001),sub);
       }

       } catch (IOException ex) {
               Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
           }
   }
   
   public void saveExchanges() {
        try {   
           f = new File(pathExchangesFile);
           if(f.exists() && f.canWrite()) {

               pw = null;
               sp = new StringBuilder(exchangesHeader).append("\n");

               for (ListPairs l : Exchanges.listExchange) {
                   if(lastExchange != null && lastExchange.equals(l.exchangeName))
                       sp.append(sub);
                   
                   sp.append(l.exchangeName).append(split);
                   sp.append(l.fee_Currency).append(split);
                   if(l.default_Fee_Percentage.signum() > 0)
                       l.default_Fee_Percentage = l.default_Fee_Percentage.movePointRight(2);
                   else
                       l.default_Fee_Percentage = BigDecimal.ZERO;
                   sp.append(l.default_Fee_Percentage).append(split);
                   sp.append(l.last_Order).append("\n");


               }
            pw = new PrintWriter(f);
            pw.print(sp);
            pw.flush();

           } 
       
        } catch (IOException  ex) {
           Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            if(pw != null)
               pw.close();
        }
       
       
   }
   
   public void setExchange(String exchange , boolean reloadData) {
       
       lp = exchanges.getExchange(exchange);
       if(lp == null) {
           return;
       }
       
       if(lp.pairData.isEmpty() || reloadData) {
           
           initData();
       }
       
       Gui.jList1.setModel(lp);
       lastExchange = exchange;
   }
   
   public void initData() {

     try {
        f = new File(path+lp.exchangeName);
        String[] pair_header; 
        if(f.exists()) {
            lf=f.listFiles();
            if(lf.length > 0) {              
                for (File lf1 : lf) {
                    pair = lf1.getName().substring(0, lf1.getName().indexOf('.'));
                    data = readCsv(lf1);
                    pair_header = data.remove(0); //headers
                    if(pair_header.length != headers.length || pair_header[pair_header.length-1] == null)
                        continue; //Error                   

                    //custom Fee Percentage for pair
                    if(pair_header[pair_header.length-1].matches("Fee=\\d+.*"))
                        fee_Percentage = getValueNum(pair_header[pair_header.length-1]).movePointLeft(2);
                    else
                        fee_Percentage = BigDecimal.ZERO;
                    
                    lp.pairData.add(new PairData(pair,data,fee_Percentage,false));                    

                }
                //Collections list to [A-Z]
                if(lp.pairData.size() > 1)
                    Collections.sort(lp.pairData);                  
                        
            }
        }
        else
           f.mkdir();
        
     } catch (IOException ex) {
           Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
       }
   } 
      
   public void setPairData(String pair) {
       
       if(lp == null)
           return;
     
        //calculate all fee_column for all pairs
        if(lp.default_Fee_Percentage.signum() > 0 && !lp.is_Fee_Calculated) {
            for(PairData p : lp.pairData){          
                if(p.pair.startsWith(lp.fee_Currency) || p.pair.endsWith(lp.fee_Currency)) { 
                    continue;                    
                }               
                //calculate Fee
                if (calculateData(p,false) && p.feeCost.signum() > 0)
                    addFeeToPair(p);            
            }                  
            lp.is_Fee_Calculated=true;           
        }
        
        PairData p = lp.getPairData(pair);
        if(p != null) {
            gui.pd=p;
            Gui.jTable1.setModel(p);
            
            if(calculateData(p,true))
                displayData();
        }      
   }
   
   public void addFeeToPair(PairData p) {
       if(p == null || lp == null)
           return;

       date_column =  p.findColumn(hdr_Date);
       if(date_column == -1)
           return;
       
       PairData pd = lp.getPairData(lp.fee_Currency+sub+reference_Currancy); // if pair not exist return
        
       if(pd != null) {
            
           for (String[] row : pd.data) { // Check if row exist
                if(row[date_column].equals(autoAdded+p.pair)) {
                    row[quantity_column]= p.fee+lp.fee_Currency;
                    row[amount_column]= p.feeCost+reference_Currancy;
                    return;
                }
           }
             //add new                 Date/Note           Side  Price       Quantity                 Amount               Fee
           pd.data.add(new String []  {autoAdded+p.pair , sell, sub, p.fee+lp.fee_Currency, p.feeCost+reference_Currancy, sub});
       }              
   }  
    
   public void removeFeeFromPair(PairData p) { // if oldPair null remove fee row
       if(p == null || lp == null)
           return;

       date_column =  p.findColumn(hdr_Date);
       if(date_column == -1)
           return;
       
       PairData pd = lp.getPairData(lp.fee_Currency+p.pair.substring(p.pair.indexOf(sub))); // if pair not exist return
        
       if(pd != null) {            
           for (String[] row : pd.data) { // Check if row exist
                if(row[date_column].equals(autoAdded+p.pair)) {
                    pd.data.remove(row);
                    break;
                }
           }
        }
       
   }
   
   public boolean calculateData(PairData p , boolean calculateAllData) {

       if(lp == null)
           return false;
       
       try {
           if(p.data.isEmpty() || p.data.get(0).length != headers.length)
              return false; 

           if(calculateAllData) {
                all_Buy  = BigDecimal.ZERO;
                all_Sell = BigDecimal.ZERO;
                quantity_Buy = BigDecimal.ZERO;
                quantity_Sell = BigDecimal.ZERO;
                quantity = BigDecimal.ZERO;
                average = BigDecimal.ZERO;
                all_Time_average = BigDecimal.ZERO;
                fee  = BigDecimal.ZERO;
            }else {
                p.fee=BigDecimal.ZERO;
                p.feeCost=BigDecimal.ZERO;       
                p.feeBuyCost=BigDecimal.ZERO;
                p.feeSellCost=BigDecimal.ZERO;
            }


           side_column = p.findColumn(hdr_Side);
           quantity_column = p.findColumn(hdr_Quantity); 
           amount_column = p.findColumn(hdr_Amount);
           fee_column = p.findColumn(hdr_Fee);
           price_column = p.findColumn(hdr_Price);
           //-1 if not found
           if(side_column == -1 || price_column == -1 || quantity_column == -1 || amount_column == -1 || fee_column == -1) //Catch any Error
               return false;

           if(p.data.get(0)[side_column] == null || 
              p.data.get(0)[quantity_column] == null || 
              p.data.get(0)[amount_column] == null)
               return false;

           if(p.data.get(0)[quantity_column].matches(value_And_Currancy))
                currancy = getValueStr(p.data.get(0)[quantity_column]);
           else {
               if(calculateAllData)
                    warnUser(0,hdr_Quantity);         
               return false;
           }

           if(p.data.get(0)[amount_column].matches(value_And_Currancy))
                reference_Currancy = getValueStr(p.data.get(0)[amount_column]);
           else {
               if(calculateAllData)
                    warnUser(0,hdr_Amount);
               return false; 
           }


           //check if added custom fee Percentage for pair
           if(p.custom_Fee_Percentage.signum() > 0)
               fee_Percentage = p.custom_Fee_Percentage;
           else
               fee_Percentage = lp.default_Fee_Percentage;


           for (String[] row : p.data) {

             if(row[side_column] == null || 
                row[quantity_column] == null || 
                row[amount_column] == null ||
                row.length != headers.length )
                 continue; //null row

             if(!row[side_column].equalsIgnoreCase(buy) && !row[side_column].equalsIgnoreCase(sell))
                 continue;

             if(!row[quantity_column].matches(value_And_Currancy) ||
                !row[amount_column].matches(value_And_Currancy))
                 continue;


             if(row[fee_column] != null && row[fee_column].matches(value_And_Currancy))
                 fee_Currancy = getValueStr(row[fee_column]);
             else
                 fee_Currancy = sub;

             if(fee_Currancy.equals(currancy) && (row[price_column] == null || !row[price_column].matches(value_Int)))
               continue; 


             //Fee
             if(!calculateAllData) {
                    
                    if(!fee_Currancy.equals(sub) && fee_Currancy.equals(lp.fee_Currency)) {   
                        fee = getValueNum(row[fee_column]);
                        if(fee.signum() > 0) {
                            p.fee = p.fee.add(fee);
                            p.feeCost = p.feeCost.add(getValueNum(row[amount_column])); 

                        if(row[side_column].equalsIgnoreCase(buy))
                            p.feeBuyCost = p.feeBuyCost.add(getValueNum(row[amount_column]));
                        else if(row[side_column].equalsIgnoreCase(sell))
                            p.feeSellCost = p.feeSellCost.add(getValueNum(row[amount_column]));

                        }  
                    }
                    continue;
                }//End Fee


             if(row[side_column].equalsIgnoreCase(buy)) {
                 all_Buy = all_Buy.add(getValueNum(row[amount_column]));                
                 quantity_Buy= quantity_Buy.add(getValueNum(row[quantity_column]));
                 quantity = quantity.add(getValueNum(row[quantity_column]));
                 average = average.add(getValueNum(row[amount_column]));

                 //Fee
                 if(!fee_Currancy.equals(sub)) {


                    if(fee_Currancy.equals(reference_Currancy)) {
                        all_Buy = all_Buy.add(getValueNum(row[fee_column]));
                        average = average.add(getValueNum(row[fee_column]));
                        fee = fee.add(getValueNum(row[fee_column]));

                    }else if (fee_Currancy.equals(currancy)) {
                        quantity_Buy = quantity_Buy.subtract(getValueNum(row[fee_column]));             
                        quantity = quantity.subtract(getValueNum(row[fee_column]));                   
                        fee = fee.add(getValueNum(row[fee_column]).multiply(getValueNum(row[price_column]), MathContext.DECIMAL32));

                    }else if (fee_Currancy.equals(lp.fee_Currency) && fee_Percentage.signum() > 0 && getValueNum(row[fee_column]).signum() > 0)
                        average = average.add(getValueNum(row[amount_column]).multiply(fee_Percentage, MathContext.DECIMAL32));

                  }//End Fee
             }
             else if(row[side_column].equalsIgnoreCase(sell)) {
                 all_Sell = all_Sell.add(getValueNum(row[amount_column]));
                 quantity_Sell= quantity_Sell.subtract(getValueNum(row[quantity_column]));

                 //Fee
                 if(!fee_Currancy.equals(sub)) {


                    if(fee_Currancy.equals(reference_Currancy)) {
                        all_Sell = all_Sell.subtract(getValueNum(row[fee_column]));
                        fee = fee.add(getValueNum(row[fee_column]));
                    }

                    else if (fee_Currancy.equals(currancy)) {               
                        quantity_Sell = quantity_Sell.subtract(getValueNum(row[fee_column]));
                        fee = fee.add(getValueNum(row[fee_column]).multiply(getValueNum(row[price_column]), MathContext.DECIMAL32));
                    }
                 }//End Fee

                 if(quantity_Buy.add(quantity_Sell).signum() != 1) {
                        average = BigDecimal.ZERO;
                        quantity = BigDecimal.ZERO;
                 }


             }   

           }

           if(calculateAllData) {

                all_Buy = all_Buy.add(p.feeBuyCost);
                all_Sell = all_Sell.subtract(p.feeSellCost);
                total = all_Sell.subtract(all_Buy);

                if(average.signum() > 0)
                        average = average.divide(quantity, MathContext.DECIMAL32);

                quantity = quantity_Buy.add(quantity_Sell);

                if(quantity.signum() > 0)
                all_Time_average = all_Buy.subtract(all_Sell).divide(quantity, MathContext.DECIMAL32);

                fee = fee.add(p.feeCost);

           }else {
                p.feeBuyCost = p.feeBuyCost.multiply(fee_Percentage, MathContext.DECIMAL32);
                p.feeSellCost = p.feeSellCost.multiply(fee_Percentage, MathContext.DECIMAL32);
                p.feeCost = p.feeCost.multiply(fee_Percentage, MathContext.DECIMAL32);
           }
       } catch (Exception e) {
                e.printStackTrace();
                return false;
       }
       return true;
   }
   
   public void warnUser(int row ,String column) {
       Gui.messageError("problem in cell format!\n row: "+row+"\n column: "+column);        
   }
   
   public void displayData() {

       Gui.all_Buy.setText(sub+all_Buy.toPlainString() +" "+reference_Currancy);      
       Gui.all_Sell.setText(all_Sell.toPlainString() +" "+reference_Currancy);
       Gui.total.setText(total.toPlainString() +" "+reference_Currancy);
       Gui.quantity_Buy.setText(quantity_Buy.toPlainString() +" "+currancy);
       Gui.quantity_Sell.setText(quantity_Sell.toPlainString() +" "+currancy);
       Gui.total_Quantity.setText(quantity.toPlainString() +" "+currancy);
       
       if(average.signum() > 0)
           Gui.average.setText(average.toPlainString() +" "+reference_Currancy);
       else
           Gui.average.setText("  -");
       
       if(quantity.signum() > 0)
           Gui.all_Time_average.setText(all_Time_average.toPlainString() +" "+reference_Currancy);
       else
           Gui.all_Time_average.setText("  -");
       
       Gui.all_fee.setText(fee.toPlainString() +" "+reference_Currancy);
       switch (total.signum()) {
           case -1:
               Gui.total.setForeground(Color.RED);
               break;
           case 1:
               Gui.total.setForeground(Color.GREEN.darker());
               break;              
           default:
               Gui.total.setForeground(Color.BLACK);
               
       }
       
       if (all_Time_average.signum() == -1)
               Gui.all_Time_average.setForeground(Color.GREEN.darker());
       else
               Gui.all_Time_average.setForeground(Color.BLACK);
       
       
   }
   
   public static void refreshData(PairData p , boolean recalculateFee) {
        if(p.data.isEmpty())
            return;
        
        if(recalculateFee) {
            m.quantity_column = p.findColumn(hdr_Quantity); 
            m.amount_column = p.findColumn(hdr_Amount);

            if(p.data.get(0)[m.quantity_column].matches(value_And_Currancy))
                 m.currancy = m.getValueStr(p.data.get(0)[m.quantity_column]);
            else
                return;
            
            if(p.data.get(0)[m.amount_column].matches(value_And_Currancy))
                 m.reference_Currancy = m.getValueStr(p.data.get(0)[m.amount_column]);
            else
                return;

            //update pair name if need
            if(!p.pair.equals(m.currancy+sub+m.reference_Currancy)) {

                if(!p.equals(gui.pd_blank)) {
                    m.f = new File(m.path+lp.exchangeName+File.separator+p.pair+m.extension);         
                    if(m.f.exists())
                        m.f.renameTo(new File(m.path+lp.exchangeName+File.separator+m.currancy+sub+m.reference_Currancy+m.extension));
                    m.removeFeeFromPair(p);                   
                }
                p.pair = m.currancy+sub+m.reference_Currancy;
            }

            if(lp.default_Fee_Percentage.signum() > 0 && !p.pair.startsWith(lp.fee_Currency) && !p.pair.endsWith(lp.fee_Currency)) {
                //calculate Fee
                if (m.calculateData(p,false) && p.feeCost.signum() > 0)
                        m.addFeeToPair(p);            
            }else {
                p.fee=BigDecimal.ZERO;
                p.feeCost=BigDecimal.ZERO;       
                p.feeBuyCost=BigDecimal.ZERO;
                p.feeSellCost=BigDecimal.ZERO;
            }
        }
        if (m.calculateData(p,true))
            m.displayData();      
   }
   
   
   private BigDecimal getValueNum(String cell) {      
       return BigDecimal.valueOf(Double.parseDouble(cell.replaceAll("[^.0-9]", "")));
   }
 
   private String getValueStr(String cell) {      
       return cell.replaceAll("[0-9.]", "").trim();
   }
   
   public int importCsv(File f) {
       if(lp == null)
           return -1;
       
       int ordersCount=0;
       try {
            String[] header;
            int pair_column=-1;       
            int feeCurrancy=-1;
            date_column=-1;
            side_column=-1;
            price_column=-1;
            quantity_column=-1;
            amount_column=-1;
            fee_column=-1;
            currancy=null;
            reference_Currancy=null;
         
            if(!f.exists() || !f.isFile() || !f.canRead())
                return -2; 
            
            sc = new Scanner(f); 
            if(sc.hasNext()) {
                String firstLine = sc.nextLine();
                if(!firstLine.contains(split) || 
                   !firstLine.contains(hdr_Side) ||
                   !firstLine.contains(hdr_Fee)) {
                    return -3; 
                }
               
            }
            sc.close();          
            data = readCsv(f);
            
            if(data.size() > 1)
                header = data.remove(0);
            else
                return -4;
            
            //Binance:
            //    0          1     2      3          4        5       6
            //"Date(UTC)","Pair","Side","Price","Executed","Amount","Fee"
            //Kucoin:  
            //  0           1           2                3               4       5          6            7             8                  9                10             11                    12                   13              14         15         16
            //"UID","Account Type","Order ID","Order Time(UTC+03:00)","Symbol","Side","Order Type","Order Price","Order Amount","Avg. Filled Price","Filled Amount","Filled Volume","Filled Volume (USDT)","Filled Time(UTC+03:00)","Fee","Fee Currency","Status"
            
            for (int i = 0 ; i < header.length ; i++) {

                if(date_column == -1 && (header[i].contains("Date") || header[i].startsWith("Filled Time")))
                    date_column = i;
                else if(pair_column == -1 && (header[i].equalsIgnoreCase("Pair") || header[i].equalsIgnoreCase("Symbol")))
                    pair_column = i;            
                else if(side_column == -1 && header[i].equalsIgnoreCase(hdr_Side))
                    side_column = i;                   
                else if(price_column == -1 && (header[i].equalsIgnoreCase(hdr_Price) || header[i].equalsIgnoreCase("Avg. Filled Price")))
                    price_column = i;                  
                else if(quantity_column == -1 && (header[i].equalsIgnoreCase(hdr_Quantity) || header[i].equalsIgnoreCase("Executed") || header[i].equalsIgnoreCase("Filled Amount")))
                    quantity_column = i;  
                else if(amount_column == -1 && (header[i].equalsIgnoreCase(hdr_Amount) || header[i].equalsIgnoreCase("Filled Volume")))
                    amount_column = i;
                else if(fee_column == -1 && header[i].equalsIgnoreCase(hdr_Fee))
                    fee_column = i;    
                else if(feeCurrancy == -1 && header[i].equalsIgnoreCase("Fee Currency"))
                    feeCurrancy = i;              
            }
            
            if(date_column == -1 || pair_column == -1 || side_column == -1 || price_column == -1 || quantity_column == -1 || amount_column == -1 || fee_column == -1) {
                return -5;
            }

            // Invert data old date to first if new date in first line like Binance
            data.sort(Comparator.comparing(a -> a[date_column]));

            //Data
            for (String[] row : data) {

                if(row.length != header.length)
                    if(ordersCount == 0)
                        return -6;
                    else
                        return -16;
                
                //Pair
                pair=null; 
                for (int i = 0; i < row.length; i++) {//trim spaces in data
                    if(i != date_column)
                        row[i] = row[i].replaceAll(" ", "");
                    else
                    row[i] = row[i].trim();
                }
                //Quantity & Amount   
                if(row[quantity_column].matches(value_And_Currancy) && 
                   row[amount_column].matches(value_And_Currancy))//Binance..
                    pair = (getValueStr(row[quantity_column])+sub+getValueStr(row[amount_column])).toUpperCase();
                
                else if(row[pair_column].contains(sub)) {//Kucoin..
                    pair=row[pair_column].toUpperCase();
                    row[quantity_column]=row[quantity_column]+pair.substring(0,pair.indexOf(sub));
                    row[amount_column]=row[amount_column]+pair.substring(pair.indexOf(sub)+1);
                }else
                   if(ordersCount == 0)
                        return -7;
                   else
                        return -17;
                
                //Side
                if(!row[side_column].equalsIgnoreCase(buy) && !row[side_column].equalsIgnoreCase(sell))
                    if(ordersCount == 0)
                        return -8;
                    else
                        return -18;
                
                //Price
                if(!row[price_column].matches(value_Int))
                    row[price_column] = row[price_column].replaceAll("[^.0-9]", "");
                
                if(!row[price_column].matches(value_Int))
                    if(ordersCount == 0)
                        return -9;
                    else
                        return -19;
                
                //Fee
                if(!row[fee_column].matches(value_And_Currancy)) {
                    if(feeCurrancy != -1) {
                      row[fee_column]=row[fee_column]+row[feeCurrancy];  
                    }else
                        if(ordersCount == 0)
                            return -10;
                        else
                            return -20;
                }

                final String[] newRow = {row[date_column],row[side_column].toUpperCase(),row[price_column],row[quantity_column],row[amount_column],row[fee_column]};     
                final PairData p = lp.getPairData(pair);
                    
                if(p != null) { // check if this pair exists                        
                    p.data.add(newRow);                        
                    if(!p.save)                            
                        p.save=true;
                        
                    }else { // add new pair
                        List<String[]> newdata = new ArrayList<String[]>();
                        newdata.add(newRow);
                        lp.pairData.add(new PairData(pair,newdata,BigDecimal.ZERO,true));
                    }
                
               ordersCount++; 
            }

           //Collections list to [A-Z]
           if(lp.pairData.size() > 1)
                Collections.sort(lp.pairData);
           
           if("Kucoin".equals(lp.exchangeName)) { //get last order date kucoin
               for (int i = 0 ; i < header.length ; i++) {
                   if(header[i].startsWith("Order Time")) {
                        String s = data.get(data.size()-1)[i];
                        if(s.contains(":"))
                            s = s.substring(0, s.indexOf(':')-2);
                        lp.last_Order = s;
                        break;
                   }
               }
           
           }else           
                lp.last_Order = data.get(data.size()-1)[date_column]; //last order date for next import
           
           saveAllData(false ,false);
           Gui.jList1.setModel(gui.lp_blank);
           Gui.jList1.setModel(lp);
           
           lp.is_Fee_Calculated=false;
      
       } catch (FileNotFoundException  ex) {
           Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
       } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
       
          if(sc != null)
              sc.close();
       }

       return ordersCount;
   }
   
   public List<String[]> readCsv(File f) throws IOException {
    List<String[]> lines = new ArrayList<>();
    FileReader fr = new FileReader(f);
    StringBuilder sb;
    int start;
    int end;
    String wrongData = "\"";
    
    
        try (BufferedReader r = new BufferedReader(fr)) {
            String line;
            while ((line = r.readLine()) != null) {
               if(line.contains(split)) {
                    if(line.contains(wrongData)) { //(,) inside Data Example: BUY,"16,100.20",0.01BTC,...
                       sb = new StringBuilder(line);
                       start = line.indexOf(wrongData);
                       end = line.lastIndexOf(wrongData); 
                       String sr = line.substring(start , end ).replace(split,"");
                       sb.replace(start, end, sr);
                       line = sb.toString().replaceAll(wrongData, "");                                    
                    }
                    lines.add(line.split(split));
                }
            }
            r.close();
            fr.close();
        }

    return lines;
}
   
   
public boolean writeCsv(ListPairs l , PairData pd) {
    if(l == null)
        return false;
    
    try {
        pw = null;
        f = new File(path+l.exchangeName+File.separator+pd.pair+extension);
         
        if(!f.exists())
            f.createNewFile();

        sp = new StringBuilder(pd.data.size());
         
        for (String w : headers)
           sp.append(w).append(split);

        sp.deleteCharAt(sp.lastIndexOf(split));
        
        if(pd.custom_Fee_Percentage.signum() > 0)
            sp.append("=").append(pd.custom_Fee_Percentage.movePointRight(2));
        
        sp.append("\n");
         
        for (String[] row : pd.data) {
            if(row[0]!= null && row[0].startsWith(autoAdded))
                continue;
            for (String cell : row) {
               if(cell != null)
                    sp.append(cell);
               
            sp.append(split);
            }  
            
            sp.deleteCharAt(sp.lastIndexOf(split));
            sp.append("\n");          
        }
         

         
        pw = new PrintWriter(f);
        pw.print(sp);
        pw.flush();
       
       } catch (IOException  ex) {           
           Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
           return false;
       }finally {
            if(pw != null)
               pw.close();
       }
    
    return true;
}


public boolean saveData (PairData pd) {
    if(lp == null)
        return false;

    if(!pd.equals(gui.pd_blank))
        return writeCsv(lp , pd);
    
    else{ //new pair
       final String[] firstRow = pd.data.get(0);
       amount_column = pd.findColumn(hdr_Amount);
       quantity_column = pd.findColumn(hdr_Quantity);
       if(firstRow[quantity_column] == null ||                 
          firstRow[amount_column] == null ||
          !firstRow[quantity_column].matches(value_And_Currancy)|| 
          !firstRow[amount_column].matches(value_And_Currancy))
           return false;

       data = new ArrayList<String[]>();
       data.addAll(pd.data);
       currancy = getValueStr(firstRow[quantity_column]).toUpperCase();
       reference_Currancy = getValueStr(firstRow[amount_column]).toUpperCase();
       
       PairData p = lp.getPairData(currancy+sub+reference_Currancy);
       if(p != null) { // check if this pair exists
          p.data.addAll(pd.data); 

       }else { //new Pair
          p =  new PairData(currancy+sub+reference_Currancy,data,BigDecimal.ZERO,false);
          lp.pairData.add(p);
          //Collections list to [A-Z]
          if(lp.pairData.size() > 1)
                    Collections.sort(lp.pairData); 
          Gui.jList1.setModel(gui.lp_blank);
          Gui.jList1.setModel(lp);
          Gui.jList1.setSelectedIndex(lp.pairData.indexOf(p)); 
       }

       
       gui.pd=p;
       Gui.jTable1.setModel(p);
       return writeCsv(lp,p);

    }
}
 
public void saveAllData (boolean allExchanges , boolean confirm) {
    
    if(lp == null)
        return;
    
    if(!allExchanges) {//Save current Exchange

        for(PairData p : lp.pairData){
            if(p.save) 
               p.save = !writeCsv(lp,p);   
        }
    }else {//Save All Exchanges
        for (ListPairs l : Exchanges.listExchange) {
            if(!l.pairData.isEmpty()) {
                
                for(PairData p : l.pairData){
                    if(p.save) {
                       if(confirm) {
                           if(!gui.confirmSaveData())
                               return;
                           confirm=false;
                       }
                       p.save = !writeCsv(l,p);
                    }                    
                    
                }
            }
                
        }        

        
    }

    if(gui.pd_blank.save) {
       if(confirm && !gui.confirmSaveData())
          return;
       saveData(gui.pd_blank);
   }    
  
}    

   
}
