package edu.aau.arena.model;

import edu.aau.ccms.Model.Role;

/** Account directory/session view. It deliberately has no credential field. */
public record UserSummary(String id, String name, String email, Role role) {
  @Override
  public String toString() {
    return name + " · " + role;
  }
}
