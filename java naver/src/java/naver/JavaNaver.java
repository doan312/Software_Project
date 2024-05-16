package com.naver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Stack;

public class JavaNaver {

    private static int zoomLevel = 16;
    private static JLabel label;
    private static String latitude;
    private static String longitude;
    private static Point markerPosition = new Point(350, 400); // Approximate center position for simplicity
    static JLayeredPane layeredPane;
    private static JButton zoomInButton;
    private static JButton zoomOutButton;
    private static Stack<JPanel> pageStack = new Stack<>(); // Stack to keep track of pages

    public static void main(String[] args) {
        JFrame frame = new JFrame("Naver Static Map Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 800);

        label = new JLabel();
        label.setBounds(0, 0, 700, 800);

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(700, 800));
        layeredPane.add(label, JLayeredPane.DEFAULT_LAYER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        zoomInButton = new JButton("Zoom In");
        zoomOutButton = new JButton("Zoom Out");
        buttonPanel.add(zoomInButton);
        buttonPanel.add(zoomOutButton);
        buttonPanel.setBounds(600, 10, 100, 60);
        layeredPane.add(buttonPanel, JLayeredPane.PALETTE_LAYER);

        frame.add(layeredPane);

        zoomInButton.addActionListener(e -> adjustZoom(1));
        zoomOutButton.addActionListener(e -> adjustZoom(-1));

        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (isNearMarker(e.getPoint())) {
                    JPanel currentPanel = new JPanel();
                    currentPanel.setLayout(null);
                    currentPanel.setBounds(0, 0, 700, 800);
                    for (Component component : layeredPane.getComponents()) {
                        currentPanel.add(component);
                    }
                    pageStack.push(currentPanel); // Push current page to stack
                    HotelBookingPopup.openHotelBookingPage();
                }
            }
        });

        label.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                if (notches < 0) {
                    adjustZoom(1);
                } else {
                    adjustZoom(-1);
                }
            }
        });

        fetchAndDisplayMap();
        frame.setVisible(true);
    }

    private static boolean isNearMarker(Point clickPoint) {
        double distance = clickPoint.distance(markerPosition);
        return distance < 50; // Check if click is within 50 pixels of the marker
    }

    public static void fetchAndDisplayMap() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ipinfo.io/json?token=85efe3742137e2"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String[] loc = response.body().split("\"loc\": \"")[1].split("\",")[0].split(",");
            latitude = loc[0];
            longitude = loc[1];

            String clientId = "5bt0m1r7kk";
            String clientSecret = "gxArtOeB8YTQrtpI5vNwZPPKaXSdcOANdDKRUhHX";
            String markers = String.format("type:d|size:mid|pos:%s%%20%s", longitude, latitude);
            String mapUrl = String.format(
                    "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?w=700&h=800&center=%s,%s&level=%d&markers=%s&X-NCP-APIGW-API-KEY-ID=%s&X-NCP-APIGW-API-KEY=%s",
                    longitude, latitude, zoomLevel, markers, clientId, clientSecret
            );

            URL url = new URL(mapUrl);
            BufferedImage image = ImageIO.read(url);
            label.setIcon(new ImageIcon(image));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void adjustZoom(int delta) {
        zoomLevel += delta;
        fetchAndDisplayMap();
    }

    public static void setZoomButtonsVisible(boolean visible) {
        zoomInButton.setVisible(visible);
        zoomOutButton.setVisible(visible);
    }

    public static void goBack() {
        if (!pageStack.isEmpty()) {
            JPanel previousPage = pageStack.pop();
            layeredPane.removeAll();
            for (Component component : previousPage.getComponents()) {
                layeredPane.add(component);
            }
            layeredPane.revalidate();
            layeredPane.repaint();
            setZoomButtonsVisible(true); // Show zoom buttons
        }
    }
}


class HotelBookingPopup {
    public static void openHotelBookingPage() {
        // Hide zoom buttons
        JavaNaver.setZoomButtonsVisible(false);

        JPanel formPanel = new JPanel();
         formPanel.setLayout(null); // Use null layout for absolute positioning
        formPanel.setPreferredSize(new Dimension(680, 1600)); // Adjusted preferred size for scrolling

        // Business Number
        JLabel bsNumLabel = new JLabel("비즈니스 넘버:");
        bsNumLabel.setBounds(20, 60, 100, 25);
        formPanel.add(bsNumLabel);
        JTextField hotelBSNumField = new JTextField(20);
        hotelBSNumField.setBounds(140, 60, 200, 25);
        formPanel.add(hotelBSNumField);

        // Hotel Name
        JLabel hotelNameLabel = new JLabel("호텔 이름:");
        hotelNameLabel.setBounds(20, 100, 100, 25);
        formPanel.add(hotelNameLabel);
        JTextField hotelNameField = new JTextField(20);
        hotelNameField.setBounds(140, 100, 200, 25);
        formPanel.add(hotelNameField);

        // Hotel Area
        JLabel hotelAreaLabel = new JLabel("지역:");
        hotelAreaLabel.setBounds(20, 140, 100, 25);
        formPanel.add(hotelAreaLabel);
        String[] areas = {"서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시", "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원도", "충청북도", "충청남도", "전라북도", "전라남도", "경상북도", "경상남도", "제주특별자치도"};
        JComboBox<String> hotelAreaComboBox = new JComboBox<>(areas);
        hotelAreaComboBox.setBounds(140, 140, 200, 25);
        formPanel.add(hotelAreaComboBox);

        // Detailed Address
        JLabel detailedAddressLabel = new JLabel("상세 주소:");
        detailedAddressLabel.setBounds(20, 180, 100, 25);
        formPanel.add(detailedAddressLabel);
        JTextField detailedAddressField = new JTextField(20);
        detailedAddressField.setBounds(140, 180, 200, 25);
        formPanel.add(detailedAddressField);

        // Number of Guests
        JLabel numGuestLabel = new JLabel("투숙객 수:");
        numGuestLabel.setBounds(20, 220, 100, 25);
        formPanel.add(numGuestLabel);
        String[] guestNumbers = {"1명", "2명", "3명", "4명", "5명", "6명"};
        JComboBox<String> numGuestComboBox = new JComboBox<>(guestNumbers);
        numGuestComboBox.setBounds(140, 220, 200, 25);
        formPanel.add(numGuestComboBox);

        // Breakfast
        JLabel breakfastLabel = new JLabel("조식 여부:");
        breakfastLabel.setBounds(20, 260, 100, 25);
        formPanel.add(breakfastLabel);
        JRadioButton breakfastYes = new JRadioButton("O");
        breakfastYes.setBounds(140, 260, 50, 25);
        JRadioButton breakfastNo = new JRadioButton("X");
        breakfastNo.setBounds(200, 260, 50, 25);
        ButtonGroup breakfastGroup = new ButtonGroup();
        breakfastGroup.add(breakfastYes);
        breakfastGroup.add(breakfastNo);
        formPanel.add(breakfastYes);
        formPanel.add(breakfastNo);

        // Room Type
        JLabel roomTypeLabel = new JLabel("룸 타입:");
        roomTypeLabel.setBounds(20, 300, 100, 25);
        formPanel.add(roomTypeLabel);
        JRadioButton suitRoom = new JRadioButton("스위트룸");
        suitRoom.setBounds(140, 300, 80, 25);
        JRadioButton deluxeRoom = new JRadioButton("디럭스룸");
        deluxeRoom.setBounds(230, 300, 80, 25);
        JRadioButton standardRoom = new JRadioButton("스탠다드룸");
        standardRoom.setBounds(320, 300, 100, 25);
        ButtonGroup roomTypeGroup = new ButtonGroup();
        roomTypeGroup.add(suitRoom);
        roomTypeGroup.add(deluxeRoom);
        roomTypeGroup.add(standardRoom);
        formPanel.add(suitRoom);
        formPanel.add(deluxeRoom);
        formPanel.add(standardRoom);

        // Hotel Cost
        JLabel hotelCostLabel = new JLabel("숙박 비용:");
        hotelCostLabel.setBounds(20, 340, 100, 25);
        formPanel.add(hotelCostLabel);
        JTextField hotelCostField = new JTextField(20);
        hotelCostField.setBounds(140, 340, 200, 25);
        formPanel.add(hotelCostField);

        // Register
        JLabel registerLabel = new JLabel("등록 여부:");
        registerLabel.setBounds(20, 380, 100, 25);
        formPanel.add(registerLabel);
        JTextField registerField = new JTextField(20);
        registerField.setBounds(140, 380, 200, 25);
        formPanel.add(registerField);

        // Hotel Photo
        JLabel hotelPhotoLabel = new JLabel("호텔 대표 사진:");
        hotelPhotoLabel.setBounds(20, 420, 100, 25);
        formPanel.add(hotelPhotoLabel);
        JButton uploadButton = new JButton("사진 업로드");
        uploadButton.setBounds(140, 420, 200, 25);
        formPanel.add(uploadButton);

// Booking Complete Button
JButton bookingCompleteButton = new JButton("예약 완료");
bookingCompleteButton.setBounds(280, 460, 120, 30); // Adjusted position to be below the hotel content
bookingCompleteButton.addActionListener(e -> {
    // Show confirmation dialog
    int response = JOptionPane.showOptionDialog(
            formPanel,
            "예약 완료되었습니다.",
            "예약 완료",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new Object[]{"확인"},
            "확인"
    );
    if (response == JOptionPane.OK_OPTION) {
        JavaNaver.goBack(); // Go back to the previous page
    }
});
formPanel.add(bookingCompleteButton);

// Checkbox for Airline Booking
JCheckBox airlineBookingCheckbox = new JCheckBox("항공편 예약");
airlineBookingCheckbox.setBounds(20, 500, 120, 25);
formPanel.add(airlineBookingCheckbox);

// Airline Form Panel (Initially Hidden)
JPanel airlineFormPanel = createAirlineForm();
airlineFormPanel.setVisible(false);
formPanel.add(airlineFormPanel);

// Checkbox for Rental Car Booking
JCheckBox rentalCarBookingCheckbox = new JCheckBox("렌터카 예약");
rentalCarBookingCheckbox.setBounds(20, 530, 120, 25); // Adjusted position
formPanel.add(rentalCarBookingCheckbox);

// Rental Car Form Panel (Initially Hidden)
JPanel rentalCarFormPanel = createRentalCarForm();
rentalCarFormPanel.setVisible(false);
formPanel.add(rentalCarFormPanel);


        // Show/Hide Airline Form based on Checkbox
        // Show/Hide Airline Form based on Checkbox
airlineBookingCheckbox.addItemListener(e -> {
    boolean airlineVisible = airlineBookingCheckbox.isSelected();
    airlineFormPanel.setVisible(airlineVisible);
    adjustFormPositions(formPanel, airlineVisible, rentalCarBookingCheckbox.isSelected(), airlineFormPanel, rentalCarFormPanel, bookingCompleteButton, rentalCarBookingCheckbox);
});

// Show/Hide Rental Car Form based on Checkbox
rentalCarBookingCheckbox.addItemListener(e -> {
    boolean rentalCarVisible = rentalCarBookingCheckbox.isSelected();
    rentalCarFormPanel.setVisible(rentalCarVisible);
    adjustFormPositions(formPanel, airlineBookingCheckbox.isSelected(), rentalCarVisible, airlineFormPanel, rentalCarFormPanel, bookingCompleteButton, rentalCarBookingCheckbox);
});


        // Back Button
 // Back Button
JButton backButton = new JButton("Back");
backButton.setBounds(20, 20, 80, 30); // Position the back button at the top left corner
backButton.addActionListener(e -> {
    JavaNaver.goBack();
});
formPanel.add(backButton);


        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBounds(0, 0, 700, 800);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JavaNaver.layeredPane.add(scrollPane, JLayeredPane.PALETTE_LAYER);
        JavaNaver.layeredPane.revalidate();
        JavaNaver.layeredPane.repaint();
    }

private static void adjustFormPositions(JPanel formPanel, boolean airlineVisible, boolean rentalCarVisible, JPanel airlineFormPanel, JPanel rentalCarFormPanel, JButton bookingCompleteButton, JCheckBox rentalCarBookingCheckbox) {
    int baseY = 560; // 기본 Y 좌표, 체크박스 바로 아래 위치

    if (airlineVisible) {
        airlineFormPanel.setBounds(20, baseY + 20, 700, 350); // Show airline form below the checkbox
        baseY += 370; // 항공편 예약 폼 높이와 패딩만큼 아래로 이동
    }

    // 렌터카 예약 체크박스 위치 조정
    rentalCarBookingCheckbox.setBounds(20, baseY + 20, 120, 25);
    baseY += 50; // 체크박스 높이와 패딩만큼 아래로 이동

    if (rentalCarVisible) {
        rentalCarFormPanel.setBounds(20, baseY + 20, 700, 350); // 렌터카 예약 폼 위치 설정
        baseY += 370; // 렌터카 예약 폼 높이와 패딩만큼 아래로 이동
    }

    // Move the booking complete button to the bottom
    bookingCompleteButton.setBounds(280, baseY + 60, 120, 30);
}



    private static JPanel createAirlineForm() {
        JPanel airlineFormPanel = new JPanel();
        airlineFormPanel.setLayout(null);

        // Business Number
        JLabel bsNumLabel = new JLabel("비즈니스 넘버:");
        bsNumLabel.setBounds(20, 0, 100, 25);
        airlineFormPanel.add(bsNumLabel);
        JTextField airplaneBSNumField = new JTextField(20);
        airplaneBSNumField.setBounds(140, 0, 200, 25);
        airlineFormPanel.add(airplaneBSNumField);

        // Airline
        JLabel airlineLabel = new JLabel("항공사:");
        airlineLabel.setBounds(20, 40, 100, 25);
        airlineFormPanel.add(airlineLabel);
        String[] airlines = {"제주 항공", "대한 항공", "에어 부산"};
        JComboBox<String> airlineComboBox = new JComboBox<>(airlines);
        airlineComboBox.setBounds(140, 40, 200, 25);
        airlineFormPanel.add(airlineComboBox);

        // Departure Area
        JLabel departureAreaLabel = new JLabel("출발 지역:");
        departureAreaLabel.setBounds(20, 80, 100, 25);
        airlineFormPanel.add(departureAreaLabel);
        String[] departureAreas = {"김포", "제주", "부산", "대구", "여수", "광주", "인천"};
        JComboBox<String> departureAreaComboBox = new JComboBox<>(departureAreas);
        departureAreaComboBox.setBounds(140, 80, 200, 25);
        airlineFormPanel.add(departureAreaComboBox);

        // Arrival Area
        JLabel arrivalAreaLabel = new JLabel("도착 지역:");
        arrivalAreaLabel.setBounds(20, 120, 100, 25);
        airlineFormPanel.add(arrivalAreaLabel);
        String[] arrivalAreas = {"김포", "제주", "부산", "대구", "여수", "광주", "인천"};
        JComboBox<String> arrivalAreaComboBox = new JComboBox<>(arrivalAreas);
        arrivalAreaComboBox.setBounds(140, 120, 200, 25);
        airlineFormPanel.add(arrivalAreaComboBox);

        // Seat Type
        JLabel seatTypeLabel = new JLabel("좌석 타입:");
        seatTypeLabel.setBounds(20, 160, 100, 25);
        airlineFormPanel.add(seatTypeLabel);
        String[] seatTypes = {"이코노미", "비즈니스", "일등석"};
        JComboBox<String> seatTypeComboBox = new JComboBox<>(seatTypes);
        seatTypeComboBox.setBounds(140, 160, 200, 25);
        airlineFormPanel.add(seatTypeComboBox);

        // Round Trip / One Way
        JLabel tripTypeLabel = new JLabel("왕복/편도:");
        tripTypeLabel.setBounds(20, 200, 100, 25);
        airlineFormPanel.add(tripTypeLabel);
        JRadioButton roundTrip = new JRadioButton("왕복");
        roundTrip.setBounds(140, 200, 60, 25);
        JRadioButton oneWay = new JRadioButton("편도");
        oneWay.setBounds(210, 200, 60, 25);
        ButtonGroup tripTypeGroup = new ButtonGroup();
        tripTypeGroup.add(roundTrip);
        tripTypeGroup.add(oneWay);
        airlineFormPanel.add(roundTrip);
        airlineFormPanel.add(oneWay);

        // Airplane Cost
        JLabel airplaneCostLabel = new JLabel("항공 비용:");
        airplaneCostLabel.setBounds(20, 240, 100, 25);
        airlineFormPanel.add(airplaneCostLabel);
        JTextField airplaneCostField = new JTextField(20);
        airplaneCostField.setBounds(140, 240, 200, 25);
        airlineFormPanel.add(airplaneCostField);

        // Register
        JLabel registerLabel = new JLabel("등록 여부:");
        registerLabel.setBounds(20, 280, 100, 25);
        airlineFormPanel.add(registerLabel);
        JTextField registerField = new JTextField(20);
        registerField.setBounds(140, 280, 200, 25);
        airlineFormPanel.add(registerField);

        // Airplane Photo
        JLabel airplanePhotoLabel = new JLabel("항공 대표 사진:");
        airplanePhotoLabel.setBounds(20, 320, 100, 25);
        airlineFormPanel.add(airplanePhotoLabel);
        JButton uploadButton = new JButton("사진 업로드");
        uploadButton.setBounds(140, 320, 200, 25);
        airlineFormPanel.add(uploadButton);

        return airlineFormPanel;
    }

    private static JPanel createRentalCarForm() {
        JPanel rentalCarFormPanel = new JPanel();
        rentalCarFormPanel.setLayout(null);

        // Business Number
        JLabel bsNumLabel = new JLabel("비즈니스 넘버:");
        bsNumLabel.setBounds(20, 0, 100, 25);
        rentalCarFormPanel.add(bsNumLabel);
        JTextField carBSNumField = new JTextField(20);
        carBSNumField.setBounds(140, 0, 200, 25);
        rentalCarFormPanel.add(carBSNumField);

        // Car Company
        JLabel carCompanyLabel = new JLabel("자동차 회사:");
        carCompanyLabel.setBounds(20, 40, 100, 25);
        rentalCarFormPanel.add(carCompanyLabel);
        JTextField carCompanyField = new JTextField(20);
        carCompanyField.setBounds(140, 40, 200, 25);
        rentalCarFormPanel.add(carCompanyField);

        // Rental Time
        JLabel rentalTimeLabel = new JLabel("대여 시간:");
        rentalTimeLabel.setBounds(20, 80, 100, 25);
        rentalCarFormPanel.add(rentalTimeLabel);
        String[] rentalTimes = {"12시간", "24시간", "30시간", "36시간", "42시간", "48시간"};
        JComboBox<String> rentalTimeComboBox = new JComboBox<>(rentalTimes);
        rentalTimeComboBox.setBounds(140, 80, 200, 25);
        rentalCarFormPanel.add(rentalTimeComboBox);

        // Car Cost
        JLabel carCostLabel = new JLabel("비용:");
        carCostLabel.setBounds(20, 120, 100, 25);
        rentalCarFormPanel.add(carCostLabel);
        JTextField carCostField = new JTextField(20);
        carCostField.setBounds(140, 120, 200, 25);
        rentalCarFormPanel.add(carCostField);

        // Vehicle Type
        JLabel vehicleTypeLabel = new JLabel("차종:");
        vehicleTypeLabel.setBounds(20, 160, 100, 25);
        rentalCarFormPanel.add(vehicleTypeLabel);
        String[] vehicleTypes = {"모닝", "레이", "아반떼", "쏘나타", "그랜저", "쏘렌토", "스타리아", "아이오닉", "코나", "레이"};
        JComboBox<String> vehicleTypeComboBox = new JComboBox<>(vehicleTypes);
        vehicleTypeComboBox.setBounds(140, 160, 200, 25);
        rentalCarFormPanel.add(vehicleTypeComboBox);

        // Fuel Type
        JLabel fuelTypeLabel = new JLabel("연료:");
        fuelTypeLabel.setBounds(20, 200, 100, 25);
        rentalCarFormPanel.add(fuelTypeLabel);
        JRadioButton gasoline = new JRadioButton("휘발유");
        gasoline.setBounds(140, 200, 60, 25);
        JRadioButton diesel = new JRadioButton("경유");
        diesel.setBounds(210, 200, 60, 25);
        JRadioButton electricity = new JRadioButton("전기");
        electricity.setBounds(280, 200, 60, 25);
        ButtonGroup fuelTypeGroup = new ButtonGroup();
        fuelTypeGroup.add(gasoline);
        fuelTypeGroup.add(diesel);
        fuelTypeGroup.add(electricity);
        rentalCarFormPanel.add(gasoline);
        rentalCarFormPanel.add(diesel);
        rentalCarFormPanel.add(electricity);

        // High Pass
        JLabel highPassLabel = new JLabel("하이패스:");
        highPassLabel.setBounds(20, 240, 100, 25);
        rentalCarFormPanel.add(highPassLabel);
        JRadioButton highPassYes = new JRadioButton("O");
        highPassYes.setBounds(140, 240, 50, 25);
        JRadioButton highPassNo = new JRadioButton("X");
        highPassNo.setBounds(200, 240, 50, 25);
        ButtonGroup highPassGroup = new ButtonGroup();
        highPassGroup.add(highPassYes);
        highPassGroup.add(highPassNo);
        rentalCarFormPanel.add(highPassYes);
        rentalCarFormPanel.add(highPassNo);

        // Register
        JLabel registerLabel = new JLabel("등록 여부:");
        registerLabel.setBounds(20, 280, 100, 25);
        rentalCarFormPanel.add(registerLabel);
        JTextField registerField = new JTextField(20);
        registerField.setBounds(140, 280, 200, 25);
        rentalCarFormPanel.add(registerField);

        // Car Photo
        JLabel carPhotoLabel = new JLabel("렌터카 대표 사진:");
        carPhotoLabel.setBounds(20, 320, 100, 25);
        rentalCarFormPanel.add(carPhotoLabel);
        JButton uploadButton = new JButton("사진 업로드");
        uploadButton.setBounds(140, 320, 200, 25);
        rentalCarFormPanel.add(uploadButton);

        return rentalCarFormPanel;
    }
}
