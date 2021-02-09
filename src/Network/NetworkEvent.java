package Network;

public interface NetworkEvent {
	void onUserAdd(Node node, long ping);
	void onUserLeave(Node node);
	void onPingUpdate(Node node, long ping);
}
