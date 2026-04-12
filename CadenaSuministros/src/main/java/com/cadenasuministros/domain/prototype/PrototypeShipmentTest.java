package com.cadenasuministros.domain.prototype;

import java.time.Instant;
import java.util.UUID;
import com.cadenasuministros.domain.model.Shipment;

public class PrototypeShipmentTest {

    public static void main(String[] args) {

        // 🔹 PROTOTIPO ORIGINAL
        Shipment original = new Shipment(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CREATED",
                "WAREHOUSE",
                Instant.now()
        );

        // 🔹 CLON 1 (cambio de estado)
        Shipment inTransit = original.withStatus("IN_TRANSIT");

        // 🔹 CLON 2 (cambio de ubicación)
        Shipment relocated = inTransit.withLocation("BOGOTÁ");

        // =============================
        // PRUEBAS POR CONSOLA
        // =============================

        System.out.println("===== PROTOTYPE TEST =====");

        System.out.println("\nORIGINAL:");
        System.out.println(original);

        System.out.println("\nCLON 1 (IN_TRANSIT):");
        System.out.println(inTransit);

        System.out.println("\nCLON 2 (RELOCATED):");
        System.out.println(relocated);

        // 🔥 PRUEBA CLAVE: referencias distintas
        System.out.println("\n¿Original == Clon 1?");
        System.out.println(original == inTransit); // false

        System.out.println("\n¿Clon 1 == Clon 2?");
        System.out.println(inTransit == relocated); // false

        // 🔥 PRUEBA CLAVE: identidad lógica
        System.out.println("\n¿Mismo ID lógico?");
        System.out.println(original.id().equals(inTransit.id())); // true
        System.out.println(inTransit.id().equals(relocated.id())); // true
    }
}