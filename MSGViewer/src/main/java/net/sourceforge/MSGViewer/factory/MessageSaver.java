package net.sourceforge.MSGViewer.factory;

import com.auxilii.msgparser.Message;
import net.sourceforge.MSGViewer.AttachmentRepository;
import net.sourceforge.MSGViewer.factory.mbox.EMLWriterViaJavaMail;
import net.sourceforge.MSGViewer.factory.mbox.MBoxWriterViaJavaMail;
import net.sourceforge.MSGViewer.factory.msg.MsgWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class MessageSaver {

    private final AttachmentRepository attachmentRepository;
    private final Message msg;

    public MessageSaver(AttachmentRepository attachmentRepository, Message msg) {
        this.attachmentRepository = attachmentRepository;
        this.msg = msg;
    }

    public void saveMessage(Path file) throws Exception {
        FileExtension extension = new FileExtension(file);

        switch (extension.toString()) {
            case "msg":
                saveMsgFile(file);
                break;
            case "mbox":
                saveMBoxFile(file);
                break;
            case "eml":
                saveEMLFile(file);
                break;
            default:
                throw new Exception("Extension '" + extension + "' not supported");
        }
    }

    private void saveMsgFile(Path file) throws IOException {
        try (OutputStream stream = Files.newOutputStream(file)) {
            MsgWriter msg_writer = new MsgWriter(msg);
            msg_writer.write(stream);
        }
    }

    private void saveMBoxFile(Path file) throws Exception {
        MBoxWriterViaJavaMail mbox_writer = new MBoxWriterViaJavaMail(attachmentRepository);
        write(mbox_writer, msg, file);
    }

    private void saveEMLFile(Path file) throws Exception {
        EMLWriterViaJavaMail eml_writer = new EMLWriterViaJavaMail(attachmentRepository);
        write(eml_writer, msg, file);
    }

    private static void write(MBoxWriterViaJavaMail writer, Message msg, Path file) throws Exception {
        try (OutputStream stream = Files.newOutputStream(file)) {
            writer.write(msg, stream);
        }
    }
}
