package cpsc4620.antonspizza;

import java.beans.*;
import java.io.*;
import java.sql.*;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/*
This file is where most of your code changes will occur
You will write the code to retrieve information from the database, or save information to the database

The class has several hard coded static variables used for the connection, you will need to change those to your connection information

This class also has static string variables for pickup, delivery and dine-in. If your database stores the strings differently (i.e "pick-up" vs "pickup") changing these static variables will ensure that the comparison is checking for the right string in other places in the program. You will also need to use these strings if you store this as boolean fields or an integer.


*/

/**
 * A utility class to help add and retrieve information from the database
 */

public final class DBNinja {

    private ArrayList<Order> orders;
    //enter your user name here
    private static String user = "NewPizzaDB_nxp3";
    //enter your password here
    private static String password = "ilikepizza2";
    //enter your database name here
    private static String database_name = "NewPizzaDB_vc4j";
    //Do not change the port. 3306 is the default MySQL port
    private static String port = "3306";
    private static Connection conn;

    //Change these variables to however you record dine-in, pick-up and delivery, and sizes and crusts
    public final static String pickup = "PICKUP";
    public final static String delivery = "DELIVERY";
    public final static String dine_in = "DINEIN";

    public final static String size_s = "S";
    public final static String size_m = "M";
    public final static String size_l = "L";
    public final static String size_xl = "XL";

    public final static String crust_thin = "thin";
    public final static String crust_orig = "original";
    public final static String crust_pan = "pan";
    public final static String crust_gf = "glutenfree";


    /**
     * This function will handle the connection to the database
     *
     * @return true if the connection was successfully made
     * @throws SQLException
     * @throws IOException
     */
    private static boolean connect_to_db() throws SQLException, IOException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load the driver");

            System.out.println("Message     : " + e.getMessage());


            return false;
        }

        conn = DriverManager.getConnection("jdbc:mysql://mysql1.cs.clemson.edu:" + port + "/" + database_name, user, password);
        return true;
    }

    /**
     * @param o order that needs to be saved to the database
     * @throws SQLException
     * @throws IOException
     * @requires o is not NULL. o's ID is -1, as it has not been assigned yet. The pizzas do not exist in the database
     * yet, and the topping inventory will allow for these pizzas to be made
     * @ensures o will be assigned an id and added to the database, along with all of it's pizzas. Inventory levels
     * will be updated appropriately
     */
    public static void addOrder(Order o) throws SQLException, IOException {
        connect_to_db();


        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()); // may need to change later - year OR adjust contraint for TSTAMP


        Pizza piz = new Pizza(-1,"S","Thin", 0.0);
        ArrayList <Pizza> newPizzaList = new ArrayList <Pizza> ();
        newPizzaList.addAll(o.getPizzas());

        Statement stmtP = conn.createStatement();
        double basep = getLocalBasePrice(piz.getSize(), piz.getCrust());
        for(int i=0;i<newPizzaList.size();i++)
        {
            Pizza tempP = newPizzaList.get(i);
            int newPID = createNewID("PID", "PIZZA");
            piz = new Pizza(newPID,tempP.getSize(),tempP.getCrust(),getLocalBasePrice(tempP.getSize(),tempP.getCrust()));

            String query2 = "insert into PIZZA (PID,STATUS,SIZE,CRUST,BID,TSTAMP) values (" + newPID + ",'In-progress','" + piz.getSize() + "','" +  piz.getCrust() + "'," + basep + ",'" + timeStamp + "');";
            try {
                int rset = stmtP.executeUpdate(query2);
            }
            catch (SQLException e) {
                System.out.println("Error inserting Pizza ID");
                while (e != null) {
                    System.out.println("Message     : " + e.getMessage());
                    e = e.getNextException();
                }
            }

        }

        int toppingID = 0;
        Discount d = getDiscount(o.getID());
        int newID = createNewID("ONUM", "ORDERS");
        o = new Order(newID,o.getCustomer(),o.getType());
        String query1 = "insert into ORDERS values (" + newID + ");";
        ArrayList <Discount> newDiscountList = new ArrayList <Discount> ();
        newDiscountList.addAll(o.getDiscounts());
        Statement q3 = conn.createStatement();
        for(int i=0;i<newDiscountList.size();i++)
        {
            Discount tempD = newDiscountList.get(i);
            int newDID = createNewID("DID", "DISCOUNT");
            d = new Discount(tempD.getName(), tempD.getPercentDisc(), tempD.getCashDisc(), tempD.getID());
            String query3 = "insert into DISCOUNT values(" + d.getID() + ",'" + d.getName() + "'," + d.getPercentDisc() + "," + d.getCashDisc() + ");";

            try {
                int rset = q3.executeUpdate(query3);
            }
            catch (SQLException e) {
                System.out.println("Error inserting DISCOUNT ID");
                while (e != null) {
                    System.out.println("Message     : " + e.getMessage());
                    e = e.getNextException();
                }
            }

        }

        Statement q1 = conn.createStatement();
        Statement q2 = conn.createStatement();

        String inv_size;
        String sz = piz.getSize();
        if(sz == size_s) inv_size = "AMTSMALL";
        else if(sz == size_m) inv_size = "AMTMED";
        else if(sz == size_l) inv_size = "AMTL";
        else inv_size = "AMTXL";

        try {
            int rset = q1.executeUpdate(query1);
        }
        catch (SQLException e) {
            System.out.println("Error Inserting Order Values");
            while (e != null) {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }
        }

        /*
        if(o.getType() == dine_in) {
            ICustomer icus = o.getCustomer();
            DineInCustomer dic = (DineInCustomer) icus;

            Statement tseats = conn.createStatement();
            Statement tabless = conn.createStatement();


            String tables_query = "Insert Into DINEIN Values(" + dic.getID() + "," + dic.getTableNum() + ");";
            String tseats_query = "Insert Into SEATNUMBERS Values(" + dic.getID() + "," + dic.getSeats() + ");";

            try {
                int rset = tseats.executeUpdate(tables_query);
            } catch (SQLException e) {
                System.out.println("Error Inserting Table Number(s)");
                while (e != null) {
                    System.out.println("Message     : " + e.getMessage());
                    e = e.getNextException();
                }
            }

            try {
                int rset = tabless.executeUpdate(tseats_query);
            } catch (SQLException e) {
                System.out.println("Error Inserting Seat Number(s)");
                while (e != null) {
                    System.out.println("Message     : " + e.getMessage());
                    e = e.getNextException();
                }
            }
        }
        */

        ArrayList <Topping> listofToppings = new ArrayList <Topping> ();
        listofToppings.addAll(piz.getToppings());


        /* Look at Inventory for the # of Toppings needed for the size of the pizza */
        for(int i=0;i<listofToppings.size();i++)
        {
            Topping new_topping = listofToppings.get(i);
            double amt = new_topping.getInv();
            int tID = new_topping.getID();
            int subtract_amt = 0;

            String calculate_inv = "Select " + inv_size + " From TOPPINGS Where TID =" + tID + ";";
            Statement invS = conn.createStatement();
            try {
                ResultSet rset = invS.executeQuery(calculate_inv);

                if(new_topping.getExtra()) subtract_amt = (rset.getInt(1)) * -2;
                else subtract_amt = (rset.getInt(1)) * -1;

                AddToInventory(new_topping,subtract_amt);
            }
            catch (SQLException e) {
                System.out.println("Error Calculating Inventory Level(s)");
                while (e != null) {
                    System.out.println("Message     : " + e.getMessage());
                    e = e.getNextException();
                }
            }
        }
        conn.close();

    }

    /**
     * @param c the new customer to add to the database
     * @throws SQLException
     * @throws IOException
     * @requires c is not null. C's ID is -1 and will need to be assigned
     * @ensures c is given an ID and added to the database
     */
    public static void addCustomer(ICustomer c) throws SQLException, IOException {
		/*add code to add the customer to the DB.
		Note: the ID will be -1 and will need to be replaced to be a fitting primary key
		Note that the customer is an ICustomer data type, which means c could be a dine in, carryout or delivery customer
		*/

        connect_to_db();

        String type = "none";
        int newID = createNewID("CID","CUSTOMER");

        String query0 = "Insert Into CUSTOMER Values (" + newID + ");";
        Statement stmt = conn.createStatement();

	/* Insert the CID into CUSTOMER */
        try {
            int rset = stmt.executeUpdate(query0);
        }
        catch (SQLException e) {
            System.out.println("Error Adding a Customer");
            while (e != null) {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }
        }

	/* Check to see the type of Customer */

        if(c instanceof DeliveryCustomer)
        {
            DeliveryCustomer dc = (DeliveryCustomer) c;
            String deliv = "Insert Into DELIVERY(PHONENO,FNAME,ADDRESS,CID) Values('" + dc.getPhone() + "','" + dc.getName() + "','" + dc.getAddress() + "'," + newID + ");";

            try { int rset = stmt.executeUpdate(deliv); }
            catch (SQLException e) {
                System.out.println("Error Adding a Customer Type - Delivery");
                while (e != null) {
                    System.out.println("Message     : " + e.getMessage());
                    e = e.getNextException();
                }
            }

            System.out.println(dc.toString());
        }
        else
        {
            DineOutCustomer doc = (DineOutCustomer) c;
            String dineout = "Insert Into PICKUP(PHONENO,FNAME,CID) Values('" + doc.getPhone() + "','" + doc.getName() + "'," + newID + ");";

            try { int rset = stmt.executeUpdate(dineout); }
            catch (SQLException e) {
                System.out.println("Error Adding a Customer Type - Pickup");
                while (e != null) {
                    System.out.println("Message     : " + e.getMessage());
                    e = e.getNextException();
                }
            }

            System.out.println(doc.toString());

        }
        conn.close();
    }

    /**
     * @param o the order to mark as complete in the database
     * @throws SQLException
     * @throws IOException
     * @requires the order exists in the database
     * @ensures the order will be marked as complete
     */
    public static void CompleteOrder(Order o) throws SQLException, IOException {
        //String query = "Select STATUS From (PIZZA as P Join BELONGSTO as BT on P.PID=BT.PID) Join ORDERS as OR on BT.ORDERNO=OR.ONUM Where OR.ONUM=" + o.getID() + ";";

        //connect_to_db();
		/*add code to mark an order as complete in the DB. You may have a boolean field for this, or maybe a completed time timestamp. However you have it, */
        String query = "Select STATUS From (PIZZA as P Join BELONGSTO as BT on P.PID=BT.PID) Join ORDERS as OR on BT.ORDERNO=OR.ONUM Where OR.ONUM=" + o.getID() + ";";

        connect_to_db();
        Statement stmt = conn.createStatement();

        try {
            ResultSet rset = stmt.executeQuery(query);
            while(rset.next())
            {
                String status = rset.getString(1);
                if(status.equals("in-progress"))
                {
                    query = "Update PIZZA Set STATUS = 'Completed' Where PID=" + o.getID() + ";";
                    rset = stmt.executeQuery(query);
                }
            }
        }
        catch (SQLException e) {
            System.out.println("Error loading Status of Order");
            while (e != null) {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }
        }
        conn.close();
    }

    /**
     * @param t     the topping whose inventory is being replenished
     * @param toAdd the amount of inventory of t to add
     * @throws SQLException
     * @throws IOException
     * @requires t exists in the database and toAdd > 0
     * @ensures t's inventory level is increased by toAdd
     */
    public static void AddToInventory(Topping t, double toAdd) throws SQLException, IOException {
        String query = "Update TOPPINGS Set INVENTORY=INVENTORY+" + toAdd + " Where TID=" + t.getID() + ";";
        // Look up Topping Name attribute to get the right attribute, currently substituting to Topping.Name
        connect_to_db();
		/*add code to add toAdd to the inventory level of T. This is not adding a new topping, it is adding a certain amount of stock for a topping. This would be used to show that an order was made to replenish the restaurants supply of pepperoni, etc*/
        Statement stmt = conn.createStatement();

        try {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println("Error updating topping inventory");
            while (e != null) {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            conn.close();
        }

    }
    /*
        A function to get the list of toppings and their inventory levels. I have left this code "complete" as an example of how to use JDBC to get data from the database. This query will not work on your database if you have different field or table names, so it will need to be changed

        Also note, this is just getting the topping ids and then calling getTopping() to get the actual topping. You will need to complete this on your own

        You don't actually have to use and write the getTopping() function, but it can save some repeated code if the program were to expand, and it keeps the functions simpler, more elegant and easy to read. Breaking up the queries this way also keeps them simpler. I think it's a better way to do it, and many people in the industry would agree, but its a suggestion, not a requirement.
    */

    /**
     * @return the List of all toppings in the database
     * @throws SQLException
     * @throws IOException
     * @ensures the returned list will include all toppings and accurate inventory levels
     */
    public static ArrayList<Topping> getInventory() throws SQLException, IOException {
        connect_to_db();
        ArrayList<Topping> ts = new ArrayList<Topping>();
        String query = "Select TID, NAME, CUSTOMER, INVENTORY From TOPPINGS;";

        Statement stmt = conn.createStatement();
        try {
            ResultSet rset = stmt.executeQuery(query);
            while(rset.next())
            {
                int ID = rset.getInt(1);
                String tname = rset.getString(2);
                double cost = rset.getDouble(3);
                int inv = rset.getInt(4);

                Topping nTop = new Topping(tname,cost,inv,ID);
                ts.add(nTop);
            }
        }
        catch (SQLException e) {
            System.out.println("Error loading inventory");
            while (e != null) {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            return ts;
        }
        //end by closing the connection
        conn.close();
        return ts;
    }

    /**
     * @return a list of all orders that are currently open in the kitchen
     * @throws SQLException
     * @throws IOException
     * @ensures all currently open orders will be included in the returned list.
     */
    public static ArrayList<Order> getCurrentOrders() throws SQLException, IOException {
        connect_to_db();
        String query = "Select BT.ORDERNO From BELONGSTO As BT, PIZZA As P Where P.PID=BT.PID and STATUS !='Completed';";
        ArrayList<Order> os = new ArrayList<Order>();
        ICustomer tempc = getICustomer(1);
        Order o = new Order(-1,tempc, "none");
        //Order O = new Order()

		/*add code to get a list of all open orders. Only return Orders that have not been completed. If any pizzas are not completed, then the order is open.*/
        Statement stmt = conn.createStatement();
        try {
            ResultSet rset = stmt.executeQuery(query);

            while (rset.next()) {
                int onum = rset.getInt(1);
                o = getOrder(onum);
                os.add(o);
                //note: pass in onum
            }
        } catch (SQLException e)
        {
            System.out.println("Error loading Current Orders");
            while (e != null) {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            //don't leave your connection open!
            conn.close();
            return os;
        }
        //end by closing the connection
        conn.close();
        return os;

    }

    /**
     * @param size  the pizza size
     * @param crust the type of crust
     * @return the base price for a pizza with that size and crust
     * @throws SQLException
     * @throws IOException
     * @requires size = size_s || size_m || size_l || size_xl AND crust = crust_thin || crust_orig || crust_pan || crust_gf
     * @ensures the base price for a pizza with that size and crust is returned
     */
    public static double getBasePrice(String size, String crust) throws SQLException, IOException {

        connect_to_db();

        String query = "Select PRICE From BASEPRICE Where SIZE ='" + size + "' And CRUST ='" + crust + "';";
        double bp = 0.0;
        //add code to get the base price for that size and crust pizza Depending on how you store size and crust in your database, you may have to do a conversion

        Statement stmt = conn.createStatement();
        //PreparedStatement ps = conn.prepareStatement(query);
        try {

            ResultSet rset = stmt.executeQuery(query);
            while (rset.next())
            {
                bp = rset.getDouble(1);
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error loading Base Price");
            while (e != null)
            {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            //don't leave your connection open!
            conn.close();
            return bp;
        }
        //end by closing the connection
        conn.close();
        return bp;
    }
    public static double getLocalBasePrice(String size, String crust) throws SQLException, IOException {

        //connect_to_db();

        String query = "Select PRICE From BASEPRICE Where SIZE ='" + size + "' And CRUST ='" + crust + "';";
        double bp = 0.0;
        //add code to get the base price for that size and crust pizza Depending on how you store size and crust in your database, you may have to do a conversion

        Statement stmt = conn.createStatement();
        //PreparedStatement ps = conn.prepareStatement(query);
        try {

            ResultSet rset = stmt.executeQuery(query);
            while (rset.next())
            {
                bp = rset.getDouble(1);
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error loading Base Price");
            while (e != null)
            {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            //don't leave your connection open!
            conn.close();
            return bp;
        }
        //end by closing the connection
        //conn.close();
        return bp;
    }

    /**
     * @return the list of all discounts in the database
     * @throws SQLException
     * @throws IOException
     * @ensures all discounts are included in the returned list
     */
    public static ArrayList<Discount> getDiscountList() throws SQLException, IOException {
        String query = "Select * From DISCOUNT;";

        ArrayList<Discount> discs = new ArrayList<Discount>();
        connect_to_db();
        //Discount d = new Discount("fake",0.00,0.0,-1 );
        //add code to get a list of all discounts

        Statement stmt = conn.createStatement();
        try {
            ResultSet rset = stmt.executeQuery(query);
            while (rset.next()) {
                //String dname = rset.getString(1);
                int DID = rset.getInt(1);
                //String n = rset.getString(2);
                //double per = rset.getDouble(3);
                //double cash = rset.getDouble(4);
                Discount d = getDiscount(DID);
                discs.add(d);
            }

        }
        catch (SQLException e) {
            System.out.println("Error loading Discount List");
            while (e != null) {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            //don't leave your connection open!
            conn.close();
            return discs;
        }
        //end by closing the connection
        conn.close();
        return discs;
    }

    /**
     * @return the list of all delivery and carry out customers
     * @throws SQLException
     * @throws IOException
     * @ensures the list contains all carryout and delivery customers in the database
     */
    public static ArrayList<ICustomer> getCustomerList() throws SQLException, IOException {
        ArrayList<ICustomer> custs = new ArrayList<ICustomer>();
        connect_to_db();
        String query_delivery = "Select FNAME,LNAME,ADDRESS,CID From DELIVERY;";
        String query_pickup = "Select FNAME,LNAME,CID From PICKUP;";

        DeliveryCustomer dc = new DeliveryCustomer(-1,"John","Doe","NotReal");
        DineOutCustomer doc = new DineOutCustomer(-1,"Jill","Doe");

        Statement stmt = conn.createStatement();

        try {
            ResultSet rset = stmt.executeQuery(query_delivery);
            while (rset.next())
            {
                String fname = rset.getString(1);
                String lname = rset.getString(2);
                String address = rset.getString(3);
                int CID = rset.getInt(4);

                dc = new DeliveryCustomer(CID,fname,lname,address);
                custs.add(dc);
            }

        }
        catch (SQLException e) {
            System.out.println("Error loading Delivery Customers");
            while (e != null) {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }
        }

        try {
            ResultSet rset = stmt.executeQuery(query_pickup);
            while (rset.next())
            {
                String fname = rset.getString(1);
                String lname = rset.getString(2);
                int CID = rset.getInt(3);

                doc = new DineOutCustomer(CID,fname,lname);
                custs.add(doc);
            }

        }
        catch (SQLException e) {
            System.out.println("Error loading Dine-Out/Pickup Customers");
            while (e != null) {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }
        }


        conn.close();
        return custs;
    }



	/*
	Note: The following incomplete functions are not strictly required, but could make your DBNinja class much simpler. For instance, instead of writing one query to get all of the information about an order, you can find the primary key of the order, and use that to find the primary keys of the pizzas on that order, then use the pizza primary keys individually to build your pizzas. We are no longer trying to get everything in one query, so feel free to break them up as much as possible

	You could also add functions that take in a Pizza object and add that to the database, or take in a pizza id and a topping id and add that topping to the pizza in the database, etc. I would recommend this to keep your addOrder function much simpler

	These simpler functions should still not be called from our menu class. That is why they are private

	We don't need to open and close the connection in these, since they are only called by a function that has opened the connection and will close it after
	*/


    private static Topping getTopping(int ID) throws SQLException, IOException {

        //add code to get a topping
        //the java compiler on unix does not like that t could be null, so I created a fake topping that will be replaced
        Topping t = new Topping("fake", 0.25, 100.0, -1);
        String query = "Select NAME, CUSTOMER, INVENTORY From TOPPINGS where NAME = " + ID + ";";

        Statement stmt = conn.createStatement();
        try {

            ResultSet rset = stmt.executeQuery(query);
            while(rset.next())
            {
                String tname = rset.getString(1);
                double price = rset.getDouble(2);
                double inv = rset.getDouble(3);

                t = new Topping(tname, price, inv, ID);
            }

        }
        catch (SQLException e) {
            System.out.println("Error loading Topping");
            while (e != null) {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            //conn.close();
            return t;
        }

        return t;
    }


    private static Discount getDiscount(int ID) throws SQLException, IOException {

        //add code to get a discount
        Discount D = new Discount("Fake",0,0,-1);
        //String query = "Select DID From APPLIEDTO where DID =" + ID + ";";
        String query2 = "Select * from DISCOUNT AS D, APPLIEDTO AS AT where AT.DID = D.DID and AT.DID=" + ID + ";";

        Statement stmt = conn.createStatement();
        try {
            ResultSet rset = stmt.executeQuery(query2);
            while(rset.next())
            {
                int DID = rset.getInt(1);
                String dname = rset.getString(2);
                double percent = rset.getDouble(3);
                double dollar = rset.getDouble(4);

                D = new Discount(dname, percent, dollar, ID);
            }
        }
        catch (SQLException e) {
            System.out.println("Error loading AppliedTo");
            while (e != null) {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            //conn.close();
            return D;
        }

        return D;

    }


    private static Pizza getPizza(int ID) throws SQLException, IOException {

        //add code to get Pizza Remember, a Pizza has toppings and discounts on it
        Pizza P = new Pizza(-1, "s", "thin", 0.0);
        Topping t = new Topping("fake", 0.25, 100.0, -1);
        Discount d = new Discount("Fake",0,0,-1);

        String query = "Select P.SIZE, P.CRUST, BP.PRICE From PIZZA As P, BASEPRICE as BP, S as PHS Where P.PID= " + ID + "And P.BID=BP.BID;";
        String query2 = "Select PHS.TID, PHS.NAME, From PIZZAHASTOPPIGS as PHS, PIZZA as P where PHS.PID=" + ID + "And PHS.PID=P.PID;";
        String query3= "Select D.DID from DISCOUNT AS D, APPLIEDTO AS A where D.DID=A.DID and A.PID=" + ID + ";";
        Statement stmt = conn.createStatement();
        try {
            ResultSet rset = stmt.executeQuery(query);

            while (rset.next()) {

                String sz = rset.getString(3);
                String crus = rset.getString(4);
                int BID = rset.getInt(5);

                P = new Pizza(ID, sz, crus, BID);
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error loading Pizza");
            while (e != null)
            {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            //conn.close();
            return P;
        }
        try
        {
            ResultSet r = stmt.executeQuery(query2);
            while(r.next())
            {
                //int TID = r.getInt(1);
                String tname = r.getString(2);

                t = getTopping(ID);
                P.addTopping(t);
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error loading Topping");
            while (e != null)
            {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            //conn.close();
            return P;
        }
        try{
            ResultSet r2 = stmt.executeQuery(query3);
            while(r2.next()){
                //int DID = r2.getInt(1);

                d = getDiscount(ID);
                P.addDiscount(d);
            }

        }
        catch (SQLException e)
        {
            System.out.println("Error loading Discount");
            while (e != null)
            {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            //conn.close();
            return P;
        }

        return P;
    }


    private static ICustomer getICustomer(int ID) throws SQLException, IOException {

        //add code to get customer

        List<Integer> seats = new ArrayList<Integer>();
        ICustomer C = new DeliveryCustomer(ID, "fake", "0000000", "Tiger Blvd");
        String checkQuery = "Select Count(*) From DINEIN Where DINEIN.CID=" + ID +";";
        String checkQuery2 = "Select Count(*) From PICKUP Where PICKUP.CID=" + ID +";";
        String checkQuery3 = "Select Count(*) From DELIVERY Where DELIVERY.CID=" + ID +";";

        String query1 = "Select D.CID, D.FNAME,D.PHONENO,D.ADDRESS from DELIVERY as D where D.CID =" + ID + ";";
        String query2 = "Select DI.CID,DI.TABLENO, SN.SEATNO from DINEIN as DI, SEATNUMBERS as SN where DI.CID =" + ID + " and DI.CID = SN.CID;";
        String query3 = "Select P.CID, P.FNAME, P.PHONENO from PICKUP as P where P.CID=" + ID + ";";

        Statement s1 = conn.createStatement();
        Statement s2 = conn.createStatement();
        Statement s3 = conn.createStatement();

        ResultSet cq = s1.executeQuery(checkQuery);
        ResultSet cq2 = s2.executeQuery(checkQuery2);
        ResultSet cq3 = s3.executeQuery(checkQuery3);
        while(cq.next())
        {
            if(cq.getInt(1) > 0)
            {
                try
                {
                    ResultSet q2 = s1.executeQuery(query2);
                    while(q2.next())
                    {
                        int tno = q2.getInt(2);
                        int seatno = q2.getInt(3);
                        seats.add(seatno);
                        C = new DineInCustomer(tno, seats, ID);
                    }

                }
                catch (SQLException e)
                {
                    System.out.println("Error loading DINEIN table from CID");
                    while (e != null)
                    {
                        System.out.println("Message     : " + e.getMessage());
                        e = e.getNextException();
                    }
                    return C;
                }
            }
        }
        while(cq2.next())
        {
            if(cq2.getInt(1) > 0)
            {
                try
                {
                    ResultSet q3 = s2.executeQuery(query3);
                    while(q3.next())
                    {
                        String name = q3.getString(2);
                        String ph = q3.getString(3);
                        C = new DineOutCustomer(ID, name, ph);
                    }
                }
                catch (SQLException e)
                {
                    System.out.println("Error loading PICKUP table from CID");
                    while (e != null)
                    {
                        System.out.println("Message     : " + e.getMessage());
                        e = e.getNextException();
                    }
                    return C;
                }
            }
        }
        while(cq3.next())
        {
            if(cq3.getInt(1) > 0)
            {
                try
                {
                    ResultSet q1 = s3.executeQuery(query1);
                    while(q1.next())
                    {
                        String name = q1.getString(2);
                        String ph = q1.getString(3);
                        String a = q1.getString(4);
                        C = new DeliveryCustomer(ID, name, ph, a);
                    }
                }
                catch (SQLException e)
                {
                    System.out.println("Error loading DELIVERY table from CID");
                    while (e != null)
                    {
                        System.out.println("Message     : " + e.getMessage());
                        e = e.getNextException();
                    }
                    return C;
                }
            }
        }

        return C;
    }

    private static Order getOrder(int OID) throws SQLException, IOException {

        //add code to get an order. Remember, an order has pizzas, a customer, and discounts on it
        String query1 = "Select BT.PID from BELONGSTO as BT Where BT.ORDERNO=" + OID +";";
        String query2 = "Select CHO.CID from CUSTOMERHASORDER as CHO where CHO.ORDERNO=" + OID +";";
        String query3 = "Select AT.DID from APPLIEDTO as AT where AT.ORDERNO=" + OID +";";
        //ArrayList<Pizza> pizzas = new ArrayList <Pizza> ();
        //ArrayList<Discount> discs = new ArrayList<Discount>();
        ICustomer tempc = getICustomer(1);
        Order O = new Order(-1,tempc, "none");
        Statement stmt1 = conn.createStatement();
        Statement stmt2 = conn.createStatement();
        Statement stmt3 = conn.createStatement();

        try {
            ResultSet rset = stmt1.executeQuery(query2);

            while (rset.next())
            {
                int cid = rset.getInt(1);
                ICustomer cust = getICustomer(cid);
                O = new Order(OID, cust,"none");
            }
        }
        catch (SQLException e)
        {
            System.out.println("Error loading Customer");
            while (e != null)
            {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            //conn.close();
            return O;
        }
        try {
            ResultSet rset = stmt2.executeQuery(query1);

            while (rset.next()) {
                int pid = rset.getInt(1);
                Pizza p = getPizza(pid);
                O.addPizza(p);
            }

        }
        catch (SQLException e)
        {
            System.out.println("Error loading Pizza");
            while (e != null)
            {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }
            //conn.close();
            return O;
        }

        try {
            ResultSet rset = stmt3.executeQuery(query3);

            while (rset.next()) {
                int did = rset.getInt(1);
                Discount d = getDiscount(did);
                O.addDiscount(d);
            }
        }
        catch (SQLException e) {
            System.out.println("Error loading Discount");
            while (e != null) {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }

            //conn.close();
            return O;
        }
        return O;

    }
    /**
     *
     * @param ID_type - either ONUM (Order Number/ID) or PID (pizza ID) or TID (Topping ID - if adding a new topping) or Cu
     * @param table - table where you are getting the ID, EX: PIZZA, TOPPINGS, CUSTOMER
     * @return maximum number + 1
     * @throws SQLException
     * @throws IOException
     */
    private static int createNewID(String ID_type, String table) throws SQLException, IOException {

        String query = "Select MAX(" + ID_type + ") FROM " + table + ";";
        int maximum = 0;
        Statement stmt = conn.createStatement();
        try {
            ResultSet rset = stmt.executeQuery(query);
            while(rset.next())
                maximum = rset.getInt(1);
        }
        catch (SQLException e)
        {
            System.out.println("Error creating a new ID for " + ID_type + " in " + table);
            while (e != null)
            {
                System.out.println("Message     : " + e.getMessage());
                e = e.getNextException();
            }
            return maximum;
        }

        return maximum + 1;
    }

}



