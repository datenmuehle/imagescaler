package org.datenmuehle.stripes.action;

import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.SimpleError;
import org.datenmuehle.persistence.Persistor;
import org.datenmuehle.persistence.ftp.FtpPersistor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@UrlBinding("/ImageScale.action")
public class ImageScaleAction implements ActionBean {

    private ActionBeanContext ctx;

    private String event;

    private List<FileBean> attachments;

    private List<Integer> hres = Arrays.asList(1024, 1600, 1980);

    private Integer selectedHRes;

    @DefaultHandler
    public Resolution onInit() {
        return new ForwardResolution("jsp/ImageScale.jsp");
    }

    public Resolution upload() {
        getContext().getValidationErrors().clear();

        String user = getContext().getRequest().getUserPrincipal().getName();

        do {
            if (attachments == null || attachments.size() == 0) {
                getContext().getValidationErrors().add("attachment", new SimpleError("Bitte mindestens eine Datei zur Übertragung auswählen."));
                break;
            }

            if (event == null)  {
                getContext().getValidationErrors().add("attachment", new SimpleError("Bitte das Ereignis bzw. die Veranstaltung angeben."));
                break;
            }

            if (selectedHRes == null) {
                getContext().getValidationErrors().add("attachment", new SimpleError("Bitte die horizontale Auflösung wählen."));
                break;
            }

            if (hres.contains(selectedHRes) == false) {
                getContext().getValidationErrors().add("attachment", new SimpleError("Bitte eine gültige horizontale Auflösung wählen."));
                break;
            }


            for (FileBean file : attachments) {
                if (file.getSize() <= 0) {
                    continue;
                }

                addFile(file, event, user);
            }
        } while (false);

        return new ForwardResolution("jsp/ImageScale.jsp");
    }

    private void addFile(FileBean file, String event, String user) {
        try {
            //file.save(new File(file.getFileName()));
            Image image = ImageIO.read(file.getInputStream());
            image = image.getScaledInstance(selectedHRes, -1, Image.SCALE_SMOOTH);

            // construct the buffered image
            BufferedImage bImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            //obtain it's graphics
            Graphics2D bImageGraphics = bImage.createGraphics();
            //draw the Image (image) into the BufferedImage (bImage)
            bImageGraphics.drawImage(image, null, null);

            // persist image
            Persistor persistor = FtpPersistor.get();
            persistor.write(bImage, event + "_" + user + "_" + file.getFileName());
        } catch (IOException e) {
            getContext().getValidationErrors().add("attachment", new SimpleError("Seems " + file.getFileName() + " isn't a valid image!"));
        }
    }

    public void setContext(ActionBeanContext actionBeanContext) {
        ctx = actionBeanContext;
    }

    public ActionBeanContext getContext() {
        return ctx;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public List<FileBean> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<FileBean> attachments) {
        this.attachments = attachments;
    }

    public List<Integer> getHres() {
        return hres;
    }

    public void setHres(List<Integer> hres) {
        this.hres = hres;
    }

    public Integer getSelectedHRes() {
        return selectedHRes;
    }

    public void setSelectedHRes(Integer selectedHRes) {
        this.selectedHRes = selectedHRes;
    }
}