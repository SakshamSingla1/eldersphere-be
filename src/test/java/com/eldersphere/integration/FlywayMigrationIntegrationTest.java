package com.eldersphere.integration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sanity check that the full migration set (V1..V35+) applies cleanly against a brand-new
 * Postgres database, matching how a fresh checkout would behave. AbstractIntegrationTest's
 * container already ran every migration by the time this context is up (Flyway runs on
 * context startup) - this test just asserts none of them failed and that the schema it
 * produced is queryable and internally consistent, rather than only inferring success from
 * other tests happening to pass.
 */
class FlywayMigrationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void allMigrations_appliedSuccessfully_againstFreshDatabase() {
        MigrationInfo[] applied = flyway.info().applied();

        assertThat(applied).isNotEmpty();
        assertThat(applied).allSatisfy(info ->
                assertThat(info.getState().isFailed())
                        .as("migration %s (%s) must not have failed", info.getVersion(), info.getDescription())
                        .isFalse());

        // The demo/seed migrations (V25, V30, V34) ran too, so the seed accounts this suite
        // and MultiRoleLoginIntegrationTest depend on actually exist.
        Integer demoAccountCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email IN (" +
                        "'superadmin@eldersphere.app', 'admin@eldersphere.app', 'caretaker.demo@eldersphere.app', " +
                        "'family.demo@eldersphere.app', 'elder.demo@eldersphere.app')",
                Integer.class);
        assertThat(demoAccountCount).isEqualTo(5);

        // The multi-role backfill (V34) gave every pre-existing user at least one mapping row.
        Integer usersWithoutRoleMapping = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users u WHERE NOT EXISTS (" +
                        "SELECT 1 FROM user_role_mappings m WHERE m.user_id = u.id)",
                Integer.class);
        assertThat(usersWithoutRoleMapping).isZero();
    }
}
