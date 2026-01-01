package pt.psoft.reader.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for Reader entity
 * Designed for mutation testing coverage
 */
@DisplayName("Reader Entity Tests")
class ReaderTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create reader with basic constructor")
        void shouldCreateReaderWithBasicConstructor() {
            Reader reader = new Reader("reader@example.com", "password123");

            assertThat(reader.getUsername()).isEqualTo("reader@example.com");
            assertThat(reader.hasRole("READER")).isTrue();
            assertThat(reader.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("Should create reader with factory method - all parameters")
        void shouldCreateReaderWithFullFactory() {
            Reader reader = Reader.newReader(
                    "reader@example.com",
                    "password123",
                    "John Doe",
                    "2025/1",
                    LocalDate.of(1990, 5, 15),
                    "+351912345678",
                    true,
                    true,
                    false
            );

            assertThat(reader.getUsername()).isEqualTo("reader@example.com");
            assertThat(reader.getFullName()).isEqualTo("John Doe");
            assertThat(reader.getReaderNumber()).isEqualTo("2025/1");
            assertThat(reader.getBirthDate()).isEqualTo(LocalDate.of(1990, 5, 15));
            assertThat(reader.getPhoneNumber()).isEqualTo("+351912345678");
            assertThat(reader.isGdprConsent()).isTrue();
            assertThat(reader.isMarketingConsent()).isTrue();
            assertThat(reader.isThirdPartyConsent()).isFalse();
        }

        @Test
        @DisplayName("Should create reader with factory method - minimal parameters")
        void shouldCreateReaderWithMinimalFactory() {
            Reader reader = Reader.newReader(
                    "reader@example.com",
                    "password123",
                    "Jane Doe",
                    "2025/2"
            );

            assertThat(reader.getUsername()).isEqualTo("reader@example.com");
            assertThat(reader.getFullName()).isEqualTo("Jane Doe");
            assertThat(reader.getReaderNumber()).isEqualTo("2025/2");
            assertThat(reader.getBirthDate()).isNull();
            assertThat(reader.getPhoneNumber()).isNull();
            assertThat(reader.isGdprConsent()).isTrue();  // Default GDPR consent
            assertThat(reader.isMarketingConsent()).isFalse();  // Default no marketing
            assertThat(reader.isThirdPartyConsent()).isFalse();  // Default no third party
        }
    }

    @Nested
    @DisplayName("Role Tests")
    class RoleTests {

        @Test
        @DisplayName("Should have READER role by default")
        void shouldHaveReaderRoleByDefault() {
            Reader reader = new Reader("reader@example.com", "password123");

            assertThat(reader.hasRole("READER")).isTrue();
            assertThat(reader.hasRole("LIBRARIAN")).isFalse();
            assertThat(reader.hasRole("ADMIN")).isFalse();
        }

        @Test
        @DisplayName("Should return correct authorities")
        void shouldReturnCorrectAuthorities() {
            Reader reader = new Reader("reader@example.com", "password123");

            assertThat(reader.getAuthorities())
                    .extracting(auth -> auth.getAuthority())
                    .contains("ROLE_READER");
        }
    }

    @Nested
    @DisplayName("Update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update phone number")
        void shouldUpdatePhoneNumber() {
            Reader reader = Reader.newReader("user@test.com", "pass", "Test User", "2025/1");

            reader.setPhoneNumber("+351987654321");

            assertThat(reader.getPhoneNumber()).isEqualTo("+351987654321");
        }

        @Test
        @DisplayName("Should update birth date")
        void shouldUpdateBirthDate() {
            Reader reader = Reader.newReader("user@test.com", "pass", "Test User", "2025/1");

            reader.setBirthDate(LocalDate.of(1985, 3, 20));

            assertThat(reader.getBirthDate()).isEqualTo(LocalDate.of(1985, 3, 20));
        }

        @Test
        @DisplayName("Should update consent flags")
        void shouldUpdateConsentFlags() {
            Reader reader = Reader.newReader("user@test.com", "pass", "Test User", "2025/1");

            reader.setMarketingConsent(true);
            reader.setThirdPartyConsent(true);

            assertThat(reader.isMarketingConsent()).isTrue();
            assertThat(reader.isThirdPartyConsent()).isTrue();
        }

        @Test
        @DisplayName("Should update reader number")
        void shouldUpdateReaderNumber() {
            Reader reader = Reader.newReader("user@test.com", "pass", "Test User", "2025/1");

            reader.setReaderNumber("2025/99");

            assertThat(reader.getReaderNumber()).isEqualTo("2025/99");
        }
    }

    @Nested
    @DisplayName("GDPR Consent Tests")
    class GdprConsentTests {

        @Test
        @DisplayName("Should track GDPR consent properly")
        void shouldTrackGdprConsentProperly() {
            Reader reader = Reader.newReader(
                    "reader@example.com",
                    "password123",
                    "John Doe",
                    "2025/1",
                    null,
                    null,
                    true,
                    false,
                    false
            );

            assertThat(reader.isGdprConsent()).isTrue();
        }

        @Test
        @DisplayName("Should default GDPR consent in minimal factory")
        void shouldDefaultGdprConsentInMinimalFactory() {
            Reader reader = Reader.newReader("user@test.com", "pass", "Test User", "2025/1");

            assertThat(reader.isGdprConsent()).isTrue();
        }
    }
}
