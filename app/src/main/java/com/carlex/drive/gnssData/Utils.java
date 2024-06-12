package com.carlex.drive.gnssData;

import java.util.Scanner;
import java.util.InputMismatchException;

public class Utils {
    private static final Scanner scanner = new Scanner(System.in);

    public static Position positionInput() {
        double latitude = -23.5000;
        double longitude = -44.666;
		double altitude = 750;

        /*while (true) {
            try {
                System.out.println("Enter position in WGS84 format (decimal degrees): ");
                System.out.print("Latitude (-90 to 90): ");
                latitude = scanner.nextDouble();
                if (latitude < -90 || latitude > 90) {
                    throw new InputMismatchException("Latitude must be between -90 and 90.");
                }
                System.out.print("Longitude (-180 to 180): ");
                longitude = scanner.nextDouble();
                if (longitude < -180 || longitude > 180) {
                    throw new InputMismatchException("Longitude must be between -180 and 180.");
                }
                scanner.nextLine(); // Consume newline
                break;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. " + e.getMessage() + " Please enter valid decimal numbers for latitude and longitude.");
                scanner.nextLine(); // Clear the invalid input
            }
        }*/
        return new Position(latitude, longitude);
    }

    public static double headingInput() {
        double heading = 45.0;
        /*while (true) {
            try {
                System.out.println("Enter heading: ");
                heading = scanner.nextDouble();
                scanner.nextLine(); // Consume newline
                break;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid decimal number for heading.");
                scanner.nextLine(); // Clear the invalid input
            }
        }*/
        return heading;
    }

    public static double speedInput() {
        double speed = 33.00;
        /*while (true) {
            try {
                System.out.println("Enter speed: ");
                speed = scanner.nextDouble();
                scanner.nextLine(); // Consume newline
                break;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid decimal number for speed.");
                scanner.nextLine(); // Clear the invalid input
            }
        }*/
        return speed;
    }
}

