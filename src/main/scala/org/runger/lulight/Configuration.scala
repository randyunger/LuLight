package org.runger.lulight

import com.escalatesoft.subcut.inject.{BindingId, NewBindingModule}

/**
  * Created by randy on 9/21/16.
  */

object BindingKeys {   // in some other file?
//object WebAnalyzerId extends BindingId
  object CurrentUserId extends BindingId
//  object MaxThreadPoolSizeId extends BindingId
}

object Configuration extends NewBindingModule(module => {
  import module._   // can now use bind directly

  import BindingKeys._  // use the Binding IDs conveniently

//  bind [Logging] idBy CurrentUserId toProvider { implicit module => Logging.apply() }

//  bind [Database] toSingle new MySQLDatabase
//  bind [Analyzer] idBy WebAnalyzerId to moduleInstanceOf [WebAnalyzer]  // module singleton
//  bind [Session] idBy CurrentUserId toProvider { WebServerSession.getCurrentUser().getSession() }
//  bind [Int] idBy MaxThreadPoolSizeId toSingle 10
//  bind [WebSearch] toModuleSingle { implicit module => new GoogleSearchService() }
})

