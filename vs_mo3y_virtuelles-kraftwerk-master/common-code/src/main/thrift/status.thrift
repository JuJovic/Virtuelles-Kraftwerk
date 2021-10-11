namespace java src.main.java.thrift

struct ComponentInfoThrift {
  1: i32 id,
  2: string name,
  3: i32 kw,
  4: bool isConsumer,
  5: string timestamp
}

struct Status {
  1: i32 produced,
  2: i32 consumed
}

struct HistoryPage {
  1: i32 numberOfPages,
  2: list<ComponentInfoThrift> data
}

service StatusService {
  Status getStatus(),
  HistoryPage getCompleteHistory(1: i32 page, 2: i32 entriesPerPage),
  HistoryPage getCompleteHistoryOfSystem(1: string centralName, 2: i32 page, 3: i32 entriesPerPage),
  ComponentInfoThrift getSingleComponentInfo(1: i32 index, 2: string centralName),
  void writeComponentInfo(1: ComponentInfoThrift info, 2: i32 index, 3: string centralName)
}