package com.bobo.storage.core.semantic;

import static java.lang.annotation.ElementType.TYPE;

import com.bobo.semantic.TestInfrastructure;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @see TestInfrastructure#getDatabase()
 */
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest(properties = {"spring.sql.init.mode=always", "spring.jpa.show-sql=true"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
public @interface RepositoryTest {}
