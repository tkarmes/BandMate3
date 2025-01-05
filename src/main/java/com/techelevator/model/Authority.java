package com.techelevator.model;

import java.util.Objects;

public final class Authority {

   private final String name;

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

   public String getName() {
      return name;
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