package com.jwtly10.aicontentgenerator.repository;

import com.jwtly10.aicontentgenerator.exceptions.DatabaseException;
import com.jwtly10.aicontentgenerator.model.Role;
import com.jwtly10.aicontentgenerator.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class UserDAOImpl implements UserDAO<User> {

    private final JdbcTemplate jdbcTemplate;

    final RowMapper<User> rowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId((rs.getInt("id")));
        user.setFirstname((rs.getString("firstname")));
        user.setLastname((rs.getString("lastname")));
        user.setEmail((rs.getString("email")));
        user.setPassword((rs.getString("password")));
        user.setRole(Role.valueOf((rs.getString("role"))));
        return user;
    };

    public UserDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> list() {
        return null;
    }

    @Override
    public void create(User user) throws DatabaseException {
        log.info("Creating user");
        String sql = "INSERT INTO users_tb (firstname, lastname, email, password, role) VALUES (?, ?, ?, ?, ?)";
        try {
            jdbcTemplate.update(sql, user.getFirstname(), user.getLastname(), user.getEmail(), user.getPassword(), user.getRole().toString());
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage());
            throw new DatabaseException("Error creating user record");
        }
    }

    @Override
    public Optional<User> get(String email) {
        String sql = "SELECT * FROM users_tb WHERE email = ?";
        User user = null;
        try {
            user = jdbcTemplate.queryForObject(sql, rowMapper, email);
        } catch (Exception e) {
            log.error("User not found");
        }

        return Optional.ofNullable(user);
    }

    @Override
    public int update(User user, int id) {
        return 0;
    }

    @Override
    public int delete(int id) {
        return 0;
    }
}
