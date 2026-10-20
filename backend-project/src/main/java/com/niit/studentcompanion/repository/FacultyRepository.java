package com.niit.studentcompanion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.niit.studentcompanion.entity.Faculty;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Integer> {

}