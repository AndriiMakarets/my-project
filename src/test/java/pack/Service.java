package pack;


import java.util.HashMap;

public class Service {

    //private final SQL sql = new SQL();
    private HashMap <String,Product> map= new HashMap();

    public Product get (String id){
        Product pr;
        if (map.containsKey(id)) return map.get(id);
        else {
            pr = SQL.sqlTest.readProductByID(id);
            map.put(id, pr);
            return pr;
        }
    }

    public void put (Product product) throws InterruptedException {
        SQL.sqlTest.iQueue.add(product);
        SQL.sqlTest.insertProduct();
    }
}
