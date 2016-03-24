package util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class ArrayStream {
    public static double[][] map( double[][] array, Function<Double, Double> mapper ) {
        double[][] result = new double[array.length][array[0].length];
        for ( int i = 0; i < array.length; i++ ) {
            for ( int j = 0; j < array[i].length; j++ ) {
                result[i][j] = mapper.apply( array[i][j] );
            }
        }
        return result;
    }

    public static <T> List<List<T>> mapToList( double[][] array, Function<Double, T> mapper ) {
        List<List<T>> result = new ArrayList<List<T>>();
        for ( int i = 0; i < array.length; i++ ) {
            List<T> row = new ArrayList<>();
            for ( int j = 0; j < array[i].length; j++ ) {
                row.add( mapper.apply( array[i][j] ) );
            }
            result.add( row );
        }
        return result;
    }

    public static <T> List<List<T>> mapToList( int[][] array, Function<Integer, T> mapper ) {
        List<List<T>> result = new ArrayList<List<T>>();
        for ( int i = 0; i < array.length; i++ ) {
            List<T> row = new ArrayList<>();
            for ( int j = 0; j < array[i].length; j++ ) {
                row.add( mapper.apply( array[i][j] ) );
            }
            result.add( row );
        }
        return result;
    }

    public static double reduce( double[][] array, BinaryOperator<Double> reducer, double initalValue ) {
        double result = initalValue;
        for ( int i = 0; i < array.length; i++ ) {
            for ( int j = 0; j < array[i].length; j++ ) {
                result = reducer.apply( result, array[i][j] );
            }
        }
        return result;
    }
}
