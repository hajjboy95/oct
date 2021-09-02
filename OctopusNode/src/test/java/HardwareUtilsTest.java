import com.octopus.node.utils.HardwareUtils;
import org.junit.Test;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;


public class HardwareUtilsTest {
    @Test
    public void testGetComputerIdentifier() {
       final String s = HardwareUtils.getComputerIdentifier();
       assert (s.length() > 0);
    }

    @Test
    public void testGetComputerIdentifierDoesntChangeAfterMultipleCalls() {
        final String firstCall = HardwareUtils.getComputerIdentifier();
        String lastCall = "";
        for(int i = 0; i<10; i++) {
            lastCall = HardwareUtils.getComputerIdentifier();
        }
        assert (firstCall.equals(lastCall));
    }

}
