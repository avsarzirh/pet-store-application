package com.avsarzirh.PetStoreApplication;

import com.avsarzirh.PetStoreApplication.entity.user.UserRole;
import com.avsarzirh.PetStoreApplication.enums.Role;
import com.avsarzirh.PetStoreApplication.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// CommandLineRunner → Spring Boot tamamen ayağa kalktıktan hemen sonra
// run() metodunu otomatik çalıştırır. Veritabanı başlangıç verisi
// (seed data) eklemek için ideal yerdir.
//
// Neden buna ihtiyaç var?
// AuthService içinde kayıt sırasında şunu yapıyoruz:
//   userRoleService.findUserRoleByRole(Role.STORE_OWNER)
// Bu method veritabanında STORE_OWNER kaydı arar. Eğer tablo boşsa
// EntityNotFoundException fırlar → "No role found: STORE_OWNER" hatası.
//
// Çözüm: Uygulama başlarken tüm rolleri bir kez veritabanına yaz.
// Eğer zaten varsa tekrar ekleme (idempotent davran).

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRoleRepository userRoleRepository;

    @Override
    public void run(String... args) {
        // Role enum'undaki tüm değerleri dolaş
        for (Role role : Role.values()) {

            // Bu rol veritabanında zaten var mı?
            boolean exists = userRoleRepository.findByRole(role).isPresent();

            if (!exists) {
                // Yoksa oluştur ve kaydet
                UserRole userRole = new UserRole();
                userRole.setRole(role);
                userRoleRepository.save(userRole);
                System.out.println("[DataInitializer] Role created: " + role);
            }
        }
        System.out.println("[DataInitializer] All roles are ready.");
    }
}
