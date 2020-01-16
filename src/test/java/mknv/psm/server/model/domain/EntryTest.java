package mknv.psm.server.model.domain;

import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mknv
 */
public class EntryTest {

    @Test
    public void getDaysLeft() {
        Entry entry = new Entry();
        
        //Should return null if the expired date parameter is null
        assertNull(entry.getDaysLeft());
        
        //Shoud return 5
        LocalDate testDate = LocalDate.now().plusDays(5);
        entry.setExpiredDate(testDate);
        
        assertEquals(entry.getDaysLeft(), Integer.valueOf(5));
    }
}
