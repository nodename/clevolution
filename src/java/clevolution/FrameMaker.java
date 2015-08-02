package clevolution;

import com.sun.imageio.plugins.png.PNGMetadata;

import mikera.gui.Frames;
import mikera.gui.JIcon;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by alan on 7/29/15.
 */
public class FrameMaker {

    private static ImageWriter getPNGImageWriter() {
        Iterator<ImageWriter> it = ImageIO.getImageWritersBySuffix("png");
        return it.next();
    }

    /**
     * This is like mikera.gui.Frames.createImageFrame but the Save As
     * action also inserts the generator string in the file's PNG metadata.
     */
    public static JFrame createImageFrame(final BufferedImage image,
                                          final String generator,
                                          final String contextName,
                                          String title) {
        final JFrame f=Frames.createFrame(title);

        JMenuBar menuBar=new JMenuBar();
        JMenu menu=new JMenu("File");
        menuBar.add(menu);
        final JMenuItem jmi=new JMenuItem("Save As...");
        menu.add(jmi);
        jmi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                FileDialog fileDialog = new FileDialog(f, "Save Image As...", FileDialog.SAVE);
                fileDialog.setFile("*.png");

                fileDialog.setVisible(true);
                String fileName = fileDialog.getFile();
                if (fileName !=null) {
                    File outputFile=new File(fileDialog.getDirectory(), fileName);
                    PNGMetadata metadata = new PNGMetadata();
                    metadata.unknownChunkType.add("gnTr");
                    metadata.unknownChunkData.add(generator.getBytes());
                    metadata.unknownChunkType.add("ctXt");
                    metadata.unknownChunkData.add(contextName.getBytes());

                    IIOImage iioImage = new IIOImage(image, null, metadata);

                    try {
                        ImageWriter imageWriter = getPNGImageWriter();
                        FileImageOutputStream output = new FileImageOutputStream(outputFile);
                        imageWriter.setOutput(output);
                        imageWriter.write(null, iioImage, null);
                        output.flush();
                        output.close();
                        imageWriter.dispose();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        JComponent c=new JIcon(image);
        c.setMinimumSize(new Dimension(image.getWidth(null),image.getHeight(null)));
        f.setMinimumSize(new Dimension(image.getWidth(null)+20,image.getHeight(null)+100));
        f.add(c);
        f.setJMenuBar(menuBar);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        return f;
    }
}
