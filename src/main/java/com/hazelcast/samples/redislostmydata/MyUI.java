package com.hazelcast.samples.redislostmydata;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import redis.clients.jedis.Jedis;

import javax.servlet.annotation.WebServlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.hazelcast.samples.redislostmydata.Styles.BACKGROUND_DARK_BLUE;
import static com.hazelcast.samples.redislostmydata.Styles.COLOR_WHITE;
import static com.hazelcast.samples.redislostmydata.Styles.FONT_20PX;
import static com.hazelcast.samples.redislostmydata.Styles.FONT_BOLD;
import static com.hazelcast.samples.redislostmydata.Styles.DATA_BORDER;
import static com.hazelcast.samples.redislostmydata.Styles.TEXT_ALIGNMENT_CENTER;
import static com.hazelcast.samples.redislostmydata.Styles.TITLE_PADDING_BOTTOM;

/**
 * This UI is the application entry point. A UI may either represent a browser window (or tab) or some part of a html page where a
 * Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be overridden to add component to the user
 * interface and initialize non-component functionality.
 */
@Push(PushMode.MANUAL)
@Theme("mytheme")
public class MyUI
        extends UI {

    private final HorizontalLayout mainLayout = new HorizontalLayout();
    private final VerticalLayout entryWrapLayout = new VerticalLayout();
    private final VerticalLayout membersWrapLayout = new VerticalLayout();
    private final VerticalLayout entryLayout = new VerticalLayout();
    private final VerticalLayout membersLayout = new VerticalLayout();
    private TextField ipField;
    private TextField portField;
    private CssLayout dataLayout;
    private ConcurrentHashMap<String, String> keys;
    private Map<String, VerticalLayout> dataMap = new HashMap<>();

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        initUI();
    }

    private void initUI() {
        mainLayout.setSizeFull();

        initWrapperLayouts();

        initEntryLayout();
        initMembersLayout();

        entryWrapLayout.addComponent(entryLayout);
        membersWrapLayout.addComponent(membersLayout);

        mainLayout.addComponents(entryWrapLayout, membersWrapLayout);
        mainLayout.setExpandRatio(entryWrapLayout, (float) 0.36);
        mainLayout.setExpandRatio(membersWrapLayout, (float) 0.64);

        setContent(mainLayout);
    }

    private void initWrapperLayouts() {
        entryWrapLayout.addStyleName(BACKGROUND_DARK_BLUE);
        entryWrapLayout.setSizeFull();
        entryWrapLayout.setSpacing(false);
        entryWrapLayout.setMargin(false);
        membersWrapLayout.addStyleName(BACKGROUND_DARK_BLUE);
        membersWrapLayout.setSizeFull();
        membersWrapLayout.setSpacing(false);
        membersWrapLayout.setMargin(false);
    }
    private void initEntryLayout() {
        entryLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        Label titleLabel = new Label("Redis lost my data!");
        titleLabel.addStyleName(FONT_BOLD);
        titleLabel.addStyleName(FONT_20PX);
        titleLabel.addStyleName(COLOR_WHITE);
        titleLabel.addStyleName(TITLE_PADDING_BOTTOM);
        entryLayout.addComponent(titleLabel);

        Label seperator = new Label();
        seperator.setContentMode(ContentMode.HTML);
        StringBuilder html = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            html.append(VaadinIcons.LINE_H.getHtml());
        }
        seperator.setValue(html.toString());
        entryLayout.addComponent(seperator);

        Button insertingData = new Button("Start inserting data");
        insertingData.addStyleName("button-icon");
        insertingData.setIcon(VaadinIcons.MAGIC);
        insertingData.addClickListener(e -> insertData());
        insertingData.setWidth(100, Unit.PERCENTAGE);
        entryLayout.addComponent(insertingData);

        Label seperator2 = new Label();
        seperator2.setContentMode(ContentMode.HTML);
        seperator2.setValue(html.toString());
        entryLayout.addComponent(seperator2);

        HorizontalLayout validateFormLayout = new HorizontalLayout();
        validateFormLayout.setWidth(100, Unit.PERCENTAGE);
        ipField = new TextField();
        ipField.setWidth(100, Unit.PERCENTAGE);
        ipField.addStyleName(TEXT_ALIGNMENT_CENTER);
        ipField.setPlaceholder("IP");
        portField = new TextField();
        portField.setPlaceholder("Port");
        portField.setWidthUndefined();
        portField.addStyleName(TEXT_ALIGNMENT_CENTER);
        portField.setWidth(100, Unit.PERCENTAGE);
        Button validateButton = new Button("Validate!");
        validateButton.addStyleName("button-icon");
        validateButton.setIcon(VaadinIcons.MAGIC);
        validateButton.addClickListener(e -> validate());
        validateButton.setWidth(100, Unit.PERCENTAGE);
        validateFormLayout.addComponents(ipField, portField, validateButton);
        entryLayout.addComponent(validateFormLayout);

        Label connectedLabel = new Label("Connected!");
        connectedLabel.addStyleName(FONT_BOLD);
        connectedLabel.addStyleName(FONT_20PX);
        connectedLabel.addStyleName(COLOR_WHITE);
        connectedLabel.addStyleName(TITLE_PADDING_BOTTOM);
        connectedLabel.setVisible(false);
        entryLayout.addComponent(connectedLabel);

    }

    private void insertData() {
        int threads = 61;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        keys = new ConcurrentHashMap<>();
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();

        executorService.submit((Runnable) () -> {
            Jedis jedis = new Jedis();
            while (true) {
                System.out.println("Entries: " + keys.size());
                String stats = jedis.info("Stats");
                System.out.println(stats.substring(stats.indexOf("sync_partial_err"), stats.indexOf("expired_keys")));
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        long x = 0;
        for (int i = 1; i < threads; i++) {
            Future<Boolean> future = executorService.submit(new RedisTask(keys, x));
            futures.add(future);
            x += 10000000;
        }

    }

    public static class RedisTask
            implements Callable<Boolean> {
        private ConcurrentHashMap<String, String> keys;
        private long i;

        public RedisTask(ConcurrentHashMap<String, String> keys, long i) {
            this.keys = keys;
            this.i = i;
        }

        public Boolean call() {
            Jedis jedis = new Jedis();
            long threshold = i + 30;

            while (true) {
                if (i > threshold) {
                    return false;
                }
                String key = i++ + "";
                try {
                    jedis.set(key, new String(new byte[10000000]));
                    keys.put(key, "v");
                } catch (Exception e) {
                    System.out.println(key);
                    return true;
                }
            }

        }
    }

    private void validate() {
        String ip = ipField.getValue();
        String port = portField.getValue();

        Jedis jedis = new Jedis(ip, Integer.parseInt(port));

        for (String key : keys.keySet()) {
            String val = jedis.get(key);
            String style = "background-greenA200";
            if (val == null){
                style = "background-red";
            }
            dataMap.get(key).setStyleName(style);
            push();
        }
    }

    private void initMembersLayout() {
        membersLayout.setDefaultComponentAlignment(Alignment.TOP_CENTER);

        Label myDataLabel = new Label("My Data");
        myDataLabel.addStyleName(COLOR_WHITE);
        myDataLabel.addStyleName(FONT_20PX);
        membersLayout.addComponent(myDataLabel);

        Button showMyDataButton = new Button("Show my Data");
        showMyDataButton.addClickListener(listener -> showMyData());
        membersLayout.addComponent(showMyDataButton);

        dataLayout = new CssLayout();
        dataLayout.setWidth(100, Unit.PERCENTAGE);
        dataLayout.addStyleName("member-internal-spacing");
        dataLayout.addStyleName(DATA_BORDER);

        membersLayout.addComponent(dataLayout);
    }

    private void showMyData() {
        for (String key : keys.keySet()) {
            VerticalLayout data = new VerticalLayout();
            data.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
            data.setWidth(32, Unit.PIXELS);
            data.setHeight(32, Unit.PIXELS);
            data.setMargin(false);
            data.addStyleName("member-padding");
            data.setStyleName(DATA_BORDER);

            Label dataLabel = new Label("+");
            dataLabel.addStyleName(FONT_BOLD);
            dataLabel.addStyleName(FONT_20PX);
            dataLabel.addStyleName(COLOR_WHITE);
            dataLabel.addStyleName(TITLE_PADDING_BOTTOM);
            data.addComponent(dataLabel);
            dataLayout.addComponent(data);

            dataMap.put(key, data);
            push();
        }
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet
            extends VaadinServlet {
    }
}
