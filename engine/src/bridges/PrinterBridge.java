package bridges;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public class PrinterBridge {


    public void printStringToFileWithTimeStamp(String toWrite, List<Consumer<String>> consumerList){
        acceptListOfConsumers(consumerList,getStringWithTimeStampAttached(toWrite));
    }

    public void acceptListOfConsumers(List<Consumer<String>> list,String str) {
        list.forEach(consumer->consumer.accept(str));
    }

    public String getStringWithTimeStampAttached(String attachMe){
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        String strDate= formatter.format(date);

        return (strDate + " - " + attachMe);
    }


}
