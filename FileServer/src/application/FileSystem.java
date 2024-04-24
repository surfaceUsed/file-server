package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.Serializable;

public interface FileSystem extends Serializable {

    void getFile(DataInputStream input, DataOutputStream output);

    void saveFile(DataInputStream input, DataOutputStream output);

    void deleteFile(DataInputStream input, DataOutputStream output);
}
