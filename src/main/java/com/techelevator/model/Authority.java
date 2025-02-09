package com.techelevator.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Authority {

   @Column(name = "authority_name")
   private String name;

   /**
    * Constructs an Authority object with the given name, ensuring it's valid.
    * @param name The name of the authority, must be either 'ROLE_ADMIN' or 'ROLE_USER'.
    * @throws IllegalArgumentException if the name is invalid.
    */
   public Authority(String name) {
      if (name == null || name.trim().isEmpty()) {
         throw new IllegalArgumentException("Authority name cannot be null or empty");
      }
      String normalizedName = name.toUpperCase();
      if (!normalizedName.equals("ROLE_ADMIN") && !normalizedName.equals("ROLE_USER")) {
         throw new IllegalArgumentException("Authority must be either 'ROLE_ADMIN' or 'ROLE_USER'");
      }
      this.name = normalizedName;
   }

   /**
    * Gets the name of this authority.
    * @return The authority name.
    */
   public String getName() {

      return name;
   }

   /**
    * Sets the name of this authority. Note: This setter is included for compatibility
    * but should not be used to change an authority's name after initialization for consistency.
    * @param name The new name for the authority.
    */
   public void setName(String name) {

      this.name = name;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Authority authority = (Authority) o;
      return name.equals(authority.name);
   }

   @Override
   public int hashCode() {
      return Objects.hash(name);
   }

   @Override
   public String toString() {
      return "Authority{" +
              "name='" + name + '\'' +
              '}';
   }
}