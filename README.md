# Quorum-based replicated Data Store

The implemented data store guarantees sequential consinstency using a quorum-based protocol. The data is represented using string key-value pairs.

### Data Store side
A node initiates the data store, setting up read and write quorum parameters and acting as the coordinator.
The coordinator waits for other nodes to join the data store or for clients to perform reads and write requests.

Other nodes can join a data store by sending a request to the coordinator, therefore distributing the data store and waiting for client requests.

### Client side
The client can contact any data store node, requesting reads, writes and multipe writes.


### The Algorithm

Replicas acquire a lock on a datastore resource everytime they receive a write request for it, preventing writes conflicts and inconsistencies.
On high traffic volumes, it has been tested how all write requests would be aborted, due to the locks.

A mitigation algorithm was developed for this reason:
- Let *A* be a replica initiating a write request on resource *X*
- Let *B* be a replica currently performing a write request on resource *X*
1. A random nonce is chosen at the beginning of every write request of replica *A*
2. When replica *A* attempts to acquire a lock and the resources is already locked by replica *B*, the nonce are compared
3. If replica *A* nonce is higher than replica *B* nonce, *A* initiates a write and *B* nonce is aborted
4. If replica *B* nonce is lower than replica *B* nonce, *A* request is aborted

