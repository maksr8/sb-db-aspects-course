package org.example.sbdbaspectscourse;

import org.example.sbdbaspectscourse.config.AppConfig;
import org.example.sbdbaspectscourse.dao.UserProfileRedisDao;
import org.example.sbdbaspectscourse.model.redis.UserProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Import({UserProfileRedisDao.class, AppConfig.class})
class UserProfileRedisDaoTest extends AbstractRedisTestcontainersSetupTest {

    @Autowired
    private UserProfileRedisDao userProfileRedisDao;

    @Test
    void testSaveAndFindProfile() {
        String id = UUID.randomUUID().toString();
        UserProfile profile = new UserProfile(id, "test@example.com", "John Doe", "Dark Mode");

        userProfileRedisDao.save(profile);

        Optional<UserProfile> found = userProfileRedisDao.findById(id);

        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("John Doe", found.get().getFullName());
        Assertions.assertEquals("Dark Mode", found.get().getPreferences());
    }

    @Test
    void testDeleteProfile() {
        String id = UUID.randomUUID().toString();
        UserProfile profile = new UserProfile(id, "delete@example.com", "Jane Doe", "Light Mode");

        userProfileRedisDao.save(profile);
        Assertions.assertTrue(userProfileRedisDao.existsById(id));

        userProfileRedisDao.deleteById(id);

        Assertions.assertFalse(userProfileRedisDao.existsById(id));
        Assertions.assertTrue(userProfileRedisDao.findById(id).isEmpty());
    }

    @Test
    void testUpdateProfile() {
        String id = UUID.randomUUID().toString();
        UserProfile profile = new UserProfile(id, "update@example.com", "Old Name", "None");
        userProfileRedisDao.save(profile);

        profile.setFullName("New Name");
        userProfileRedisDao.update(profile);

        Optional<UserProfile> found = userProfileRedisDao.findById(id);
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("New Name", found.get().getFullName());
    }

    @Test
    void testFindAllProfiles() {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        userProfileRedisDao.save(new UserProfile(id1, "user1@example.com", "User 1", "N/A"));
        userProfileRedisDao.save(new UserProfile(id2, "user2@example.com", "User 2", "N/A"));

        List<UserProfile> all = userProfileRedisDao.findAll();

        Assertions.assertTrue(all.size() >= 2);
        boolean containsUser1 = all.stream().anyMatch(p -> p.getId().equals(id1));
        boolean containsUser2 = all.stream().anyMatch(p -> p.getId().equals(id2));
        Assertions.assertTrue(containsUser1);
        Assertions.assertTrue(containsUser2);
    }
}