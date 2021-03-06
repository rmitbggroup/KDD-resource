package rTreeIndex;

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import database.DatabaseManager;
import fileIO.FilePath;
import fileIO.MyFileReader;
import fileIO.RTree.Serialize;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lancer on 19/07/2018.
 */
public class IndexConstructor {


    public RTree<String, Point> rTree = RTree.star().create();

    public final int routeSize = 200000; //200k

    public static void main(String[] args) {

        IndexConstructor constructor = new IndexConstructor();

        try {
            constructor.constructRTree();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Serialize.serialize(constructor.rTree, FilePath.tripRTreePath);
    }


    private void constructRTree() throws SQLException {

        MyFileReader myFileReader = new MyFileReader("./trip-ids-to-request-route.txt");

        String line = myFileReader.getNextLine();

        List<Integer> routeIDs = new ArrayList<>();


        while (line != null && routeSize > routeIDs.size()) {
            routeIDs.add(Integer.valueOf(line));
        }

        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.connectLA();

        for (int i = 0; i < routeIDs.size(); i++) {

            if (routeIDs.size() > 0)
                databaseManager.executeQuery("select * from rawtripgoogleroutecoor where routeID = " + routeIDs.get(i) + ";");
            else
                databaseManager.executeQuery("select * from rawtripgoogleroutecoor;");

            while (databaseManager.resultSet.next()) {

                String id = "" + databaseManager.resultSet.getInt(1) + "~" + databaseManager.resultSet.getInt(2);
                BigDecimal longitude = databaseManager.resultSet.getBigDecimal(3);
                BigDecimal latitude = databaseManager.resultSet.getBigDecimal(4);

                rTree = rTree.add(id, Geometries.point(longitude.doubleValue(), latitude.doubleValue()));

            }

            if (i % 1000 == 0)
                System.out.println("finish " + i);
        }

        databaseManager.close();
    }


}