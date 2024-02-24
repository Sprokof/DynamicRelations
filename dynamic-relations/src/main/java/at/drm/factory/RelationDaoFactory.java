package at.drm.factory;

import at.drm.dao.RelationDao;
import at.drm.exception.NoRelationDaoFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@SuppressWarnings("rawtypes")
public class RelationDaoFactory {

    private final ApplicationContext applicationContext;

    public RelationDao getDaoFromSourceObjectClass(Class dynamicRelactionClass) {
        Map<String, RelationDao> beansOfType = applicationContext.getBeansOfType(RelationDao.class);
        return beansOfType.values().stream()
                .filter(dao -> {
                    ResolvableType resolvableType = ResolvableType.forClass(dao.getClass())
                            .as(RelationDao.class);
                    ResolvableType generic = resolvableType.getGeneric(0);
                    Class<?> resolve = generic.resolve();
                    assert resolve != null;
                    Field sourceObject = getDeclaredField(resolve);
                    Class<?> type = sourceObject.getType();
                    return type.equals(dynamicRelactionClass);
                }).findFirst().orElseThrow(() -> new NoRelationDaoFoundException("No DynamicRelationDao was found!"));
    }

    public Set<RelationDao> getAllDaos() {
        Map<String, RelationDao> beansOfType = applicationContext.getBeansOfType(RelationDao.class);
        return new HashSet<>(beansOfType.values());
    }

    private Field getDeclaredField(Class<?> resolve) {
        try {
            return resolve.getDeclaredField("sourceObject");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
