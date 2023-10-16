# Quorum-based replicated Data Store

The implemented data store guarantees sequential consinstency using a quorum-based protocol. The data is represented using string key-value pairs.

# Data Store side
A node initiates the data store, setting up read and write quorum parameters and acting as the coordinator.
The coordinator waits for other nodes to join the data store or for clients to perform reads and write requests.

Other nodes can join a data store by sending a request to the coordinator, therefore distributing the data store and waiting for client requests.

# Client side
The client can contact any data store node, requesting reads, writes and multipe writes.
