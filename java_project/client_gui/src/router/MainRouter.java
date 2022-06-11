package router;

import java.io.IOException;
import java.util.ArrayList;

import application.Connector;
import application.Main;
import application.SceneLoader;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import network_dto.NetworkData;
import service.MemberService;
import service.ScheduleService;
import vo.Member;
import vo.Schedule;

public class MainRouter {
	NetworkData<?> data;

	public static Stage stage;

	static MemberService memberService = new MemberService();
	static ScheduleService scheduleService = new ScheduleService();

	public NetworkData<?> route(NetworkData<?> data) {
		String action = data.getAction();
		Object value = data.getV();

		if (Main.loginMember == null) {
			Member m = null;
			if (value instanceof Member) {
				m = (Member) value;
			}
			switch (action) {
			case "member/login":
				login(m);
				break;
			case "member/join":
				break;
			}
		} else {
			String actionClass = action.split("/")[0];
			switch (actionClass) {
			case "member":
				memberRoute(data);
				break;
			case "schedule":
				scheduleRoute(data);
				break;
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private void scheduleRoute(NetworkData<?> data) {
		String action = data.getAction().split("/")[1];
		int result = 0;
		switch (action) {
		case "find":
			ArrayList<Schedule> scheList = (ArrayList<Schedule>) data.getV();
			scheduleService.getAllSchedule(scheList);
			break;
		case "save":
			result = (Integer) data.getV();
			if (result == 1) {
				Connector.send(new NetworkData<Member>("schedule/find", Main.loginMember));
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("일정 등록 실패");
				alert.setHeaderText("일정 등록에 실패했습니다.");
				alert.show();
			}
			break;

		case "findByNo":
			Schedule schedule = (Schedule) data.getV();
			scheduleService.getDetailSchedule(schedule);
			break;
		case "update":
			result = (Integer) data.getV();
			if (result == 1) {
//				Connector.send(new NetworkData<Member>("schedule/find", Main.loginMember));
				AnchorPane monthCal = (AnchorPane) Main.sceneLoader.load(SceneLoader.M_SCHEDULE_PATH);
				BorderPane borderPane = (BorderPane) ScheduleService.border;
				ScheduleService.setCalendar(monthCal);
				Platform.runLater(() -> {
					borderPane.setCenter(monthCal);
				});
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("일정 등록 실패");
				alert.setHeaderText("일정 등록에 실패했습니다.");
				alert.show();
			}
			break;
		case "delete":
			result = (Integer) data.getV();
			if (result == 1) {
				Platform.runLater(() -> {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("일정 삭제");
					alert.setHeaderText("일정 삭제 완료.");
					alert.show();
					alert.setOnCloseRequest(ev -> {
						AnchorPane monthCal = (AnchorPane) Main.sceneLoader.load(SceneLoader.M_SCHEDULE_PATH);
						BorderPane borderPane = (BorderPane) ScheduleService.border;
						ScheduleService.setCalendar(monthCal);
						borderPane.setCenter(monthCal);
					});
				});

			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("일정 삭제 실패");
				alert.setHeaderText("일정 등록에 실패했습니다.");
				alert.show();
			}
			break;
		}

	}

	private void memberRoute(NetworkData<?> data) {
		String action = data.getAction().split("/")[1];
		switch (action) {
		case "frdList":
			ArrayList<Member> frdList = (ArrayList<Member>) data.getV();
			memberService.frdList(frdList);
			break;
		}
	}

	private void login(Member member) {
		Main.loginMember = member;
		try {
			BorderPane userMain = FXMLLoader.load(getClass().getResource("../view/UserMain.fxml"));
			Platform.runLater(() -> {
				stage.setScene(new Scene(userMain));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
