package com.example;

import com.example.model.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;

public class App {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("gestion-reservations-pu");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();

            Utilisateur user = new Utilisateur("alice@example.com", "Dupont", "Alice");
            Salle salle = new Salle("Salle A", 30, "Bâtiment Principal", 2);

            Equipement projecteur = new Equipement("Projecteur", "Projecteur HD", 2);
            salle.addEquipement(projecteur);

            Reservation r1 = new Reservation(
                    LocalDateTime.of(2025, 6, 10, 9, 0),
                    LocalDateTime.of(2025, 6, 10, 11, 0),
                    "Réunion de projet", 12);
            Reservation r2 = new Reservation(
                    LocalDateTime.of(2025, 6, 11, 14, 0),
                    LocalDateTime.of(2025, 6, 11, 16, 0),
                    "Atelier", 20);

            salle.addReservation(r1);
            salle.addReservation(r2);
            user.addReservation(r1);
            user.addReservation(r2);

            em.persist(user);
            em.getTransaction().commit();

            Long userId = user.getId();
            System.out.println("=== Données créées ===");
            System.out.println("Réservations de l'utilisateur : " + user.getReservations().size());

            em.clear();

            System.out.println("=== Test 1 : suppression d'une réservation (orphelin) ===");
            em.getTransaction().begin();
            Utilisateur u1 = em.find(Utilisateur.class, userId);
            Reservation res = u1.getReservations().iterator().next();
            Salle s1 = res.getSalle();
            s1.removeReservation(res);
            u1.removeReservation(res);
            em.getTransaction().commit();

            Long nbRes1 = em.createQuery("select count(r) from Reservation r", Long.class).getSingleResult();
            System.out.println("Réservations restantes en base : " + nbRes1);

            em.clear();

            System.out.println("=== Test 2 : suppression de l'utilisateur (cascade) ===");
            em.getTransaction().begin();
            Utilisateur u2 = em.find(Utilisateur.class, userId);
            for (Reservation r : u2.getReservations()) {
                r.getSalle().getReservations().remove(r);
            }
            em.remove(u2);
            em.getTransaction().commit();

            Long nbUsers = em.createQuery("select count(u) from Utilisateur u", Long.class).getSingleResult();
            Long nbRes2 = em.createQuery("select count(r) from Reservation r", Long.class).getSingleResult();
            Long nbSalles = em.createQuery("select count(s) from Salle s", Long.class).getSingleResult();
            Long nbEquip = em.createQuery("select count(e) from Equipement e", Long.class).getSingleResult();

            System.out.println("Utilisateurs : " + nbUsers);
            System.out.println("Réservations : " + nbRes2);
            System.out.println("Salles : " + nbSalles);
            System.out.println("Équipements : " + nbEquip);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
            emf.close();
        }
    }
}