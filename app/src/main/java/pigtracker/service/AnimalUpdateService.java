package pigtracker.service;

import pigtracker.dao.AnimalDAO;
import pigtracker.model.Animal;

public class AnimalUpdateService {
    public static void updateAnimalNumber(Object row, Object newValue, Runnable callback) {
        if (row instanceof Animal animal && newValue instanceof Integer newNumber) {
            Animal updated = new Animal(animal.id(), newNumber, animal.responder(), animal.groupId(), animal.location(),
                    animal.status(), animal.stoppedReason(), animal.stoppedAt(), animal.fcr(), animal.startWeightKg(),
                    animal.totalFeedKg(), animal.weightGainKg(), animal.latestWeightKg(), animal.completedDays(),
                    animal.startDay(), animal.createdAt());
            try {
                AnimalDAO.update(updated);
                callback.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
