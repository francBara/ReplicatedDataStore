# Quorum-based replicated Data Store

The implemented data store guarantees sequential consinstency using a quorum-based protocol. The data is represented using string key-value pairs.

### Data Store side
A node initiates the data store, setting up read and write quorum parameters and acting as the coordinator.
The coordinator waits for other nodes to join the data store or for clients to perform reads and write requests.

Other nodes can join a data store by sending a request to the coordinator, therefore distributing the data store and waiting for client requests.

### Client side
The client can contact any data store node, requesting reads, writes and multipe writes.


### The Algorithm

- let *wq* the write quorum
- let *rq* the read quorum

#### Write request

- Let *C* be a client initiating a write request
- Let *A* be a node of a datastore
- let *S* a set of *wq* nodes of the datastore
- let *R* be a resource of the datastore
1. *C* contacts *A*, asks to perform a write on resource *R*
2. *A* locks *R* and contacts *S* nodes
3. If *S* lock *R*, they ack *A* and send the most recent version of *R*, if present
4. *A* collects the results and send the most recent version of *R* to *S*
5. *S* send a commit ack to *A*
6. *A* sends global commit to *S*

Whenever a client 

#### Locking

Replicas acquire a lock on a datastore resource everytime they receive a write request for it, preventing writes conflicts and inconsistencies.
On high traffic volumes, it has been tested how all write requests would be aborted, due to the locks.

A mitigation algorithm was developed for this reason:
- Let *A* be a node initiating a write request on resource *X*
- Let *B* be a node currently performing a write request on resource *X*
- Let *R* be a resource of the datastore
1. *A* choses a random nonce at the beginning of the write request
2. When node *A* attempts to acquire a lock and the resource *R* is already locked by node *B*, the nonces are compared
3. If node *A* nonce is higher than node *B* nonce, *A* initiates a write and *B* nonce is aborted
4. If node *B* nonce is lower than node *B* nonce, *A* request is aborted

Before sending a commit message, a node *X* definitively locks a resource *R*, preventing its locked to be taken over by the nonce algorithm.

