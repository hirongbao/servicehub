package com.shirongbao.admin;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
@Component
public class DbDumpRunner implements CommandLineRunner {
    private final JdbcTemplate jdbc;
    public DbDumpRunner(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== DB DUMP ===");
        List<Map<String, Object>> users = jdbc.queryForList("SELECT * FROM site_user");
        System.out.println("Users: " + new ObjectMapper().writeValueAsString(users));
        List<Map<String, Object>> ann = jdbc.queryForList("SELECT * FROM site_anniversary");
        System.out.println("Anniversaries: " + new ObjectMapper().writeValueAsString(ann));
        System.out.println("=== DB DUMP END ===");
    }
}
