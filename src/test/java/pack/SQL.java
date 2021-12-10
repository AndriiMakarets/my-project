package pack;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

//@SpringBootApplication
//@EnableCaching
public class SQL {
    private Connection con;
    public Queue<Product> iQueue = new ConcurrentLinkedQueue<Product>();

    public void create(String name){
        try{
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:memory:" + name);
            PreparedStatement st = con.prepareStatement("create table if not exists 'product' ("
        +"'id' INTEGER PRIMARY KEY AUTOINCREMENT,"
                    +" 'name' text,"
                    + "'price'double,"
                    +"'amount' double"
                    +");");

            st.executeUpdate();
            st.close();

             st = con.prepareStatement("create table if not exists 'users' ("
                    +"'id' INTEGER PRIMARY KEY AUTOINCREMENT,"
                    +" 'login' text  ,"
                    +" 'password' text "
                    +");");

            st.executeUpdate();
            st.close();

        }catch(ClassNotFoundException e){
            System.out.println("Неправильний драйвер JDBC");
            e.printStackTrace();
            System.exit(0);
        }catch (SQLException e){
            System.out.println("Неправильний SQL запит");
            e.printStackTrace();
        }
    }


    public List<Product> read(){
        try (Statement st = con.createStatement();
             ResultSet res = st.executeQuery("SELECT * FROM product");
        ){
            List<Product> products = new ArrayList<>();
            while (res.next()) {
                products.add(new Product(res.getInt("id"),res.getString("name"), res.getDouble("price"), res.getDouble("amount")));
            }
            res.close();

            return products;
        }catch(SQLException e){
            System.out.println("Неправильний SQL запит на вибірку даних");

            throw new RuntimeException("Error selecting products", e);
        }
    }

   /* public User readUserByLogin(String login){
        try (Statement st = con.createStatement();
             ResultSet res = st.executeQuery("SELECT * FROM users WHERE login ='"+login+"'");
        ){
            if(res.next()){
                return new User(res.getInt("id"), res.getString("login"), res.getString("password"));
            }
            return null;
        }catch(SQLException e){
            System.out.println("Неправильний SQL запит на вибірку даних");

            throw new RuntimeException("Error selecting products", e);
        }
    }

    public User readUserByID(String id){
        try (Statement st = con.createStatement();
             ResultSet res = st.executeQuery("SELECT * FROM users WHERE id ='"+id+"'");
        ){
            if(res.next()){
                return new User(res.getInt("id"), res.getString("login"), res.getString("password"));
            }
            return null;
        }catch(SQLException e){
            System.out.println("Неправильний SQL запит на вибірку даних");

            throw new RuntimeException("Error selecting products", e);
        }
    }*/
    public Product readProductByID(String id){
        while (true) {
            try (Statement st = con.createStatement();
                 ResultSet res = st.executeQuery("SELECT * FROM product WHERE id ='" + id + "'");
            ) {
                if (res.next()) {
                    System.out.println("у");
                    return new Product(res.getInt("id"), res.getString("name"), res.getDouble("price"), res.getDouble("amount"));
                }
                return null;
            } catch (SQLException e) {
                System.out.println("Неправильний SQL запит на вибірку даних");

                throw new RuntimeException("Error selecting products", e);
            }
        }
    }

    public Product insertProduct() throws InterruptedException {
        Product product = iQueue.peek();
        try
            (PreparedStatement statement = con.prepareStatement("INSERT INTO product(name,price, amount) VALUES (?,?,?)")){
            statement.setString(1, product.getName());
            statement.setDouble(2, product.getPrice());
            statement.setDouble(3, product.getAmount());

          statement.executeUpdate();
           ResultSet resultSet = statement.getGeneratedKeys();

           product.setId(resultSet.getInt("last_insert_rowid()"));
            return product;
        }catch (SQLException e){
            System.out.println("Неправильний SQL запит на вставку");
            e.printStackTrace();
            throw new RuntimeException("can`t insert product", e);
        }
    }

    /*public User insertUser(User user) throws SQLException {
        try
                (PreparedStatement statement = con.prepareStatement("INSERT INTO users(login, password) VALUES (?,?)")){
            statement.setString(1, user.getLogin());
            statement.setString(2, user.getPassword());


            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            user.setId(resultSet.getInt("last_insert_rowid()"));
            return user;
        }catch (SQLException e){
            System.out.println("Неправильний SQL запит на вставку");
            e.printStackTrace();
            throw new RuntimeException("can`t insert product", e);
        }
    }*/

    public void update(String name, double price, double amount, ProductFilter filter){
        StringBuilder sb = new StringBuilder();
        int count = countFilters(filter);
        if (filter.getId()!=null){
            sb.append("id = ");
            sb.append(filter.getId());
            return;
        }
        if(filter.getName()!=null) {
            sb.append(" name like '%")
                    .append(filter.getName())
                    .append("%' ");
            if (count > 1) {
                sb.append(" and ");
                count--;
            }
        }
        if(filter.getPriceFrom()!=null) {
            sb.append(" price >= ").append(filter.getPriceFrom());
            if (count > 1) {
                sb.append(" and ");
                count--;
            }
        }
        if(filter.getPriceTo()!=null) {
            sb.append(" price < ").append(filter.getPriceTo());
            if (count > 1) {
                sb.append(" and ");
                count--;
            }
        }
        if(filter.getAmountFrom()!=null) {
            sb.append(" amount >= ").append(filter.getAmountFrom());
            if (count > 1) {
                sb.append(" and ");
                count--;
            }
        }
        if(filter.getAmountTo()!=null) {
            sb.append(" amount < ").append(filter.getAmountTo());
            if (count > 1) {
                sb.append(" and ");
                count--;
            }
        }
        try
                (PreparedStatement statement = con.prepareStatement("UPDATE product SET name =?, price=?, amount=? WHERE"+ sb) ){
            statement.setString(1, name);
            statement.setDouble(2, price);
            statement.setDouble(3, amount);

            statement.executeUpdate();

        }catch (SQLException e){
            System.out.println("Неправильний SQL запит на оновлення");
            e.printStackTrace();
            throw new RuntimeException("can`t update product", e);
        }
    }

    public void updateProductByID(int id, String name, double price, double amount) throws SQLException  {

                PreparedStatement statement = con.prepareStatement("UPDATE product SET name =?, price=?, amount=? WHERE id=" + id);
            statement.setString(1, name);
            statement.setDouble(2, price);
            statement.setDouble(3, amount);

            statement.executeUpdate();

    }
    public void deleteAll (){
        try
                (PreparedStatement statement = con.prepareStatement("delete from product")){
            statement.executeUpdate();
        }catch (SQLException e){
            System.out.println("Неправильний SQL запит на вставку");
            e.printStackTrace();
            throw new RuntimeException("can`t insert product", e);
        }
    }

    public void delete (int id){
        try
                (PreparedStatement statement = con.prepareStatement("delete from product WHERE id ="+id)){
            statement.executeUpdate();
        }catch (SQLException e){
            System.out.println("Неправильний SQL запит на вставку");
            e.printStackTrace();
            throw new RuntimeException("can`t insert product", e);
        }
    }



    public List<Product> listByCriteria(ProductFilter filter){
        StringBuilder sb = new StringBuilder();
        int count = countFilters(filter);
        if(filter.getName()!=null) {
            sb.append(" name like '%")
                    .append(filter.getName())
                    .append("%' ");
            if (count > 1) {
                sb.append(" and ");
                count--;
            }
        }
        if(filter.getPriceFrom()!=null) {
            sb.append(" price >= ").append(filter.getPriceFrom());
            if (count > 1) {
                sb.append(" and ");
                count--;
            }
        }
        if(filter.getPriceTo()!=null) {
            sb.append(" price < ").append(filter.getPriceTo());
            if (count > 1) {
                sb.append(" and ");
                count--;
            }
        }
        if(filter.getAmountFrom()!=null) {
            sb.append(" amount >= ").append(filter.getAmountFrom());
            if (count > 1) {
                sb.append(" and ");
                count--;
            }
        }
        if(filter.getAmountTo()!=null) {
            sb.append(" amount < ").append(filter.getAmountTo());
            if (count > 1) {
                sb.append(" and ");
                count--;
            }
        }
        System.out.println(sb);
        try (Statement st = con.createStatement();
             ResultSet res = st.executeQuery("SELECT * FROM product WHERE "+ sb);
        ){
            List<Product> products = new ArrayList<>();
            while (res.next()) {
                products.add( new Product(res.getInt("id"),res.getString("name"), res.getDouble("price"), res.getDouble("amount")));
            }
            res.close();
            return products;
        }catch(SQLException e){
            System.out.println("Неправильний SQL запит на вибірку даних");

            throw new RuntimeException("Error selecting products", e);
        }
    }

    private int countFilters(ProductFilter filter){
        int count=0;
        if(filter.getAmountFrom()!=null)
            count++;
        if(filter.getAmountTo()!=null)
            count++;
        if(filter.getPriceFrom()!=null)
            count++;
        if(filter.getPriceTo()!=null)
            count++;
        if(filter.getName()!=null)
            count++;

return count;
    }

    public static SQL sqlTest = new SQL();
    public static void main(String[] args) throws SQLException, InterruptedException {

        Service serv = new Service();
        sqlTest.create("Product");
        serv.put(new Product("prod1", 14.5, 4.0));
        serv.put(new Product("prod2", 25.0, 6.0));
        //sqlTest.insertUser(new User("Andri", "Makaret"));
        System.out.println( serv.get("1"));
        System.out.println( serv.get("1"));
        System.out.println( serv.get("2"));
       //System.out.println(sqlTest.readProductByID("1"));
        System.out.println( serv.get("1"));//отримуємо за айді
       //System.out.println( sqlTest.read());
    }

}
